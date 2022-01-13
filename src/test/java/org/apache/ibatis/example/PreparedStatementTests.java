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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Williami
 * @description
 * @date 2022/1/10
 */
public class PreparedStatementTests {

  private static final Logger LOGGER = LoggerFactory.getLogger(PreparedStatementTests.class);


  @DisplayName("预编译与SQL注入")
  @Test
  public void testPreparedStatement() throws IOException, SQLException {
    // 解析mybatis全局配置文件
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsStream("mybatis-config-with-datasource.xml"));
    // 关闭自动提交
    SqlSession sqlSession = sqlSessionFactory.openSession();
    try (Connection connection = sqlSession.getConnection()) {
      String sql = "select * from tb_production where id = ?";
      // 将SQL语句提交给数据库进行预编译
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setString(1, "4");
      preparedStatement.executeQuery();

      //preparedStatement.setString(1, "3");
      //int executeUpdate2 = preparedStatement.executeUpdate();
      //sqlSession.commit();
    }


    // 关闭自动提交
    SqlSession sqlSession2 = sqlSessionFactory.openSession();
    try (Connection connection = sqlSession2.getConnection()) {
      String sql = "select * from tb_production where id = ?";
      // 将SQL语句提交给数据库进行预编译
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setString(1, "4");
      preparedStatement.executeQuery();

      //preparedStatement.setString(1, "3");
      //int executeUpdate2 = preparedStatement.executeUpdate();
    }


  }


  @Test
  public void testWith$() throws IOException, SQLException {
    // 解析mybatis全局配置文件
    SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsStream("mybatis-config-with-datasource.xml"));
    // 关闭自动提交
    try (SqlSession sqlSession = sqlSessionFactory.openSession();) {
      Production production = new Production();
      production.setId(15);
      production.setName("'XuGeTang'");
      // 执行SQL时进行变量替换
      sqlSession.update("org.apache.ibatis.example.ProductionMapperV2.update", production);
    }

  }

}
