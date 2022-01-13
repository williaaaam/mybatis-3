/*
 *    Copyright 2009-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.cache.decorators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

/**
 * 二级缓存
 * The 2nd level cache transactional buffer.
 * <p>
 * This class holds all cache entries that are to be added to the 2nd level cache during a Session.
 * Entries are sent to the cache when commit is called or discarded if the Session is rolled back.
 * Blocking cache support has been added. Therefore any get() that returns a cache miss
 * will be followed by a put() so any lock associated with the key can be released.
 *
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public class TransactionalCache implements Cache {

  private static final Log log = LogFactory.getLog(TransactionalCache.class);
  // 二级缓存对象
  private final Cache delegate;
  private boolean clearOnCommit;
  // 所有待提交事务的缓存
  // entriesToAddCommit在事务开启到提交期间作为真实缓存的替代品，将从数据库中查询到的数据先放到这个Map中，待事务提交后，
  // 再将这个对象中的数据刷新到真实缓存中，如果事务提交失败了，则清空这个缓存中的数据即可，并不会影响到真实的缓存。
  private final Map<Object, Object> entriesToAddOnCommit;
  // 未命中缓存
  // entriesMissedInCache主要是用来保存在查询过程中在缓存中没有命中的key，由于没有命中，说明需要到数据库中查询，那么查询过后会保存到entriesToAddCommit中，那么假设在事务提交过程中失败了，而此时entriesToAddCommit的数据又都刷新到缓存中了，
  // 那么此时调用rollback就会通过entriesMissedInCache中保存的key，来清理真实缓存，这样就可以保证在事务中缓存数据与数据库的数据保持一致。
  private final Set<Object> entriesMissedInCache;

  public TransactionalCache(Cache delegate) {
    this.delegate = delegate;
    this.clearOnCommit = false;
    this.entriesToAddOnCommit = new HashMap<>();
    this.entriesMissedInCache = new HashSet<>();
  }

  @Override
  public String getId() {
    return delegate.getId();
  }

  @Override
  public int getSize() {
    return delegate.getSize();
  }

  /**
   * 如果发现缓存中取出的数据为null，那么会把这个key放到entriesMissedInCache中，这个对象的主要作用就是将我们未命中的key全都保存下来，防止缓存被击穿，并且当我们在缓存中无法查询到数据，那么就有可能到一级缓存和数据库中查询，那么查询过后会调用putObject()方法，这个方法本应该将我们查询到的数据put到真是缓存中，但是现在由于存在事务，所以暂时先放到entriesToAddOnCommit中。
   *
   * 如果发现缓存中取出的数据不为null，那么会查看事务提交标识(clearOnCommit)是否为true，如果为true，代表事务已经提交了，之后缓存会被清空，所以返回null，如果为false，那么由于事务还没有被提交，所以返回当前缓存中存的数据。
   * @param key
   *          The key
   * @return
   */
  @Override
  public Object getObject(Object key) {
    // issue #116
    Object object = delegate.getObject(key);
    if (object == null) {
      // 如果取出的是空，那么放到未命中缓存，防止缓存穿透，我们在缓存中无法查询到数据，那么就有可能到一级缓存和数据库中查询，
      // 那么查询过后会调用putObject()方法，这个方法本应该将我们查询到的数据put到真实缓存中，但是现在由于存在事务，所以暂时先放到entriesToAddOnCommit中。
      entriesMissedInCache.add(key);
    }
    // issue #146
    if (clearOnCommit) {
      // 查看缓存清空标识是否为false，如果事务提交了就为true，事务提交了会更新缓存，所以返回null
      return null;
    } else {
      // 如果事务没有提交，那么返回原先缓存中的数据
      return object;
    }
  }

  @Override
  public void putObject(Object key, Object object) {
    // 如果返回的数据为null，那么有可能到数据库查询，查询到的数据先放置到待提交事务的缓存中
    // 本来应该put到缓存中，现在put到待提交事务的缓存中去
    entriesToAddOnCommit.put(key, object);
  }

  @Override
  public Object removeObject(Object key) {
    return null;
  }

  @Override
  public void clear() {
    // 如果事务提交了，那么将清空缓存提交标识设置为true
    clearOnCommit = true;
    // 清空事务提交缓存
    entriesToAddOnCommit.clear();
  }

  public void commit() {
    // 如果为true，那么就清空缓存
    if (clearOnCommit) {
      // 清空二级缓存数据
      delegate.clear();
    }
    // 把未提交的事务缓存 entriesToAddOnCommit刷新到真实缓存
    flushPendingEntries();
    //然后将所有值复位
    reset();
  }

  public void rollback() {
    //事务回滚
    unlockMissedEntries();
    reset();
  }

  private void reset() {
    //复位操作
    clearOnCommit = false;
    entriesToAddOnCommit.clear();
    entriesMissedInCache.clear();
  }

  private void flushPendingEntries() {
    // 遍历事务管理器中待提交的缓存
    for (Map.Entry<Object, Object> entry : entriesToAddOnCommit.entrySet()) {
      // 写入到真实的缓存中
      delegate.putObject(entry.getKey(), entry.getValue());
    }
    for (Object entry : entriesMissedInCache) {
      // 把未命中的一起put
      if (!entriesToAddOnCommit.containsKey(entry)) {
        // 未命中缓存设置为null
        delegate.putObject(entry, null);
      }
    }
  }

  private void unlockMissedEntries() {
    for (Object entry : entriesMissedInCache) {
      try {
        // 清空真实缓存区中未命中的缓存
        // 由于凡是在缓存中未命中的key，都会被记录到entriesMissedInCache这个缓存中，所以这个缓存中包含了所有查询数据库的key，所以最终只需要在真实缓存中把这部分key和对应的value给删除即可
        delegate.removeObject(entry);
      } catch (Exception e) {
        log.warn("Unexpected exception while notifying a rollback to the cache adapter. "
            + "Consider upgrading your cache adapter to the latest version. Cause: " + e);
      }
    }
  }

}
