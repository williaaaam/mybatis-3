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
package org.apache.ibatis.scripting.xmltags;

/**
 * 简单理解就是xml中每个标签，例如<update> <trim>等
 * @author Clinton Begin
 */
public interface SqlNode {
  /**
   *
   * @param context 将各Sql片段合并到DynamicContext中，拼接称为完整的SQL
   * @return
   */
  boolean apply(DynamicContext context);
}
