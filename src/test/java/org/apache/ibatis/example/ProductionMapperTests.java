/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.example;

import org.apache.ibatis.domain.Production;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.StringTokenizer;

/**
 * @author Williami
 * @description
 * @date 2022/1/8
 */
public class ProductionMapperTests {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProductionMapperTests.class);


  @DisplayName("测试自定义ProductionMapper")
  @Test
  public void testCustomProductionMapper() throws IOException {
    String resource = "mybatis-config.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    // 查找数据源、解析执行SQL
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    // 创建执行器
    try (SqlSession sqlSession = sqlSessionFactory.openSession();) {
      // 操作数据库
      int count = sqlSession.selectOne("org.apache.ibatis.example.ProductionMapper.countUserWithNullableIsFalse", Arrays.asList(1, 2));
      //sqlSession.commit();

      // 缓存
      //Production production2 = sqlSession.selectOne("org.apache.ibatis.example.ProductionMapper.selectOne", 1L);
      //LOGGER.info(">>> production = {}", production);
      //LOGGER.info(">>> production2 = {}", production2);

      //
      //ProductionMapper productionMapper = sqlSession.getMapper(ProductionMapper.class);
      //LOGGER.info(">>> production3 = {}", productionMapper.selectPK());

    }

  }

  @Test
  public void testSelectOneByNameWithCustomTypeHandler() throws IOException {
    String resource = "mybatis-config.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    // 查找数据源、解析执行SQL
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    // SqlSession调用close后就会销毁一级缓存
    try (SqlSession sqlSession = sqlSessionFactory.openSession();) {
      Production production = sqlSession.selectOne("org.apache.ibatis.example.ProductionMapper.selectOneByName", "Apple");
      System.out.println(production);
    }

  }

  @DisplayName("移除首尾 空格 制表符 回车符和换行符 \f\t\n\r")
  @Test
  public void testRemoveExtraWhitespaces() {
    String sql = " select * from tb_production ";
    System.out.println(removeExtraWhitespaces(sql));
  }


  public static String removeExtraWhitespaces(String original) {
    StringTokenizer tokenizer = new StringTokenizer(original);
    StringBuilder builder = new StringBuilder();
    boolean hasMoreTokens = tokenizer.hasMoreTokens();
    while (hasMoreTokens) {
      builder.append(tokenizer.nextToken());
      hasMoreTokens = tokenizer.hasMoreTokens();
      if (hasMoreTokens) {
        builder.append(' ');
      }
    }
    return builder.toString();
  }


  @Test
  public void testSelectOneByIdAndNameWithCustomTypeHandler() throws IOException {
    String resource = "mybatis-config.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    // 查找数据源、解析执行SQL
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    // 创建执行器
    try (SqlSession sqlSession = sqlSessionFactory.openSession();) {
      // Mybatis将new Object[]{5, "Apple"}包装成集合，源码如下：
      /**
       *       ParamMap<Object> map = new ParamMap<>();
       *       map.put("array", object);
       */
      Production production = sqlSession.selectOne("org.apache.ibatis.example.ProductionMapper.selectOneByIdAndName", new Object[]{5, "Apple"});
      System.out.println(production);
    }

  }

  @Test
  public void testSelectProduction() throws IOException {
    String resource = "mybatis-config.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    // 查找数据源、解析执行SQL
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
    // 需要手动提交事务
    try (SqlSession sqlSession = sqlSessionFactory.openSession(false)) {
      /*Production production = new Production();
      production.setName("Porsche3");
      sqlSession.insert("org.apache.ibatis.example.ProductionMapper.insert", production);*/
      Production o = sqlSession.selectOne("org.apache.ibatis.example.ProductionMapper.selectOneByName", "Porsche3");
      System.out.println(o);
    }
    // 隐式执行SqlSession#close,回滚TransactionManager#rollBack(),清空entriesToAddOnCommit和entriesMissedInCache
  }

  @DisplayName("测试二级缓存效果，不提交事务，sqlSession1查询完数据后，sqlSession2相同的查询是否会从缓存中获取数据")
  @Test
  public void testCacheWiehoutCommitOrClose() throws IOException {

    String resource = "mybatis-config.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    // 查找数据源、解析执行SQL
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

    SqlSession sqlSession1 = sqlSessionFactory.openSession(true);
    SqlSession sqlSession2 = sqlSessionFactory.openSession(true);
    SqlSession sqlSession3 = sqlSessionFactory.openSession(true);

    Production production1 = sqlSession1.selectOne("org.apache.ibatis.example.ProductionMapper.selectOneByName", "Porsche2");
    // 当没有sqlSession1没有调用commit（）时，二级缓存没有起到作用
    // 如果SqlSession没有关闭，调用commit还会清空一级缓存中的数据，同时将entriesToAddOnCommit和entriesMissedInCache缓存一起刷新到真实缓存，然后清空entriesToAddOnCommit和entriesMissedInCache
    sqlSession1.close(); // 实验效果等同于sqlSession1.commit()，但是一级缓存失效localCache=null
    Production production2 = sqlSession2.selectOne("org.apache.ibatis.example.ProductionMapper.selectOneByName", "Porsche2");
    Production production3 = sqlSession3.selectOne("org.apache.ibatis.example.ProductionMapper.selectOneByName", "Porsche2");

    LOGGER.info(">>> SqlSession1 查询Production1 = {}", production1); // 缓存命中率 0.0
    LOGGER.info(">>> SqlSession2 查询Production2 = {}", production2); // 缓存命中率 0.5, 1/(1+1)=0.5
    LOGGER.info(">>> SqlSession3 查询Production3 = {}", production3); // 缓存命中率=1/(1+1+1)=0.66666666666

  }


  @DisplayName("测试先查询、提交，后更新，再查询的二级缓存效果")
  @Test
  public void testSelectAndUpdateWithCache() throws IOException {

    String resource = "mybatis-config.xml";
    InputStream inputStream = Resources.getResourceAsStream(resource);
    // 查找数据源、解析执行SQL
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

    SqlSession sqlSession1 = sqlSessionFactory.openSession(true);
    SqlSession sqlSession2 = sqlSessionFactory.openSession(true);
    SqlSession sqlSession3 = sqlSessionFactory.openSession(true);

    Production production1 = sqlSession1.selectOne("org.apache.ibatis.example.ProductionMapper.selectOneByName", "Porsche2");
    sqlSession1.commit();

    Production production = new Production();
    production.setName("MyBatis-Source");
    production.setId(12);
    sqlSession2.update("org.apache.ibatis.example.ProductionMapper.update", production);
    sqlSession2.commit();// 如果没有执行commit,则update之后二级缓存依然有效


    Production production3 = sqlSession3.selectOne("org.apache.ibatis.example.ProductionMapper.selectOneByName", "Porsche2");

    LOGGER.info(">>> SqlSession1 查询Production1 = {}", production1);
    //LOGGER.info(">>> SqlSession2 查询Production2 = {}", production2);
    LOGGER.info(">>> SqlSession3 查询Production3 = {}", production3);

  }


}
