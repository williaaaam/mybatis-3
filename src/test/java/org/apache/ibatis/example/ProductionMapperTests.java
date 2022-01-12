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
    // 创建执行器
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


}
