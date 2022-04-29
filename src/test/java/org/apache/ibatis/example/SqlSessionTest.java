package org.apache.ibatis.example;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.domain.blog.Section;
import org.apache.ibatis.domain.blog.mappers.AuthorMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.attachment.AttachmentUnmarshaller;
import java.io.Reader;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * @author william
 * @title
 * @desc
 * @date 2022/4/29
 **/
public class SqlSessionTest extends BaseDataTest {

    private static SqlSessionFactory sqlMapper;

    @BeforeClass
    public static void setup() throws Exception {
        createBlogDataSource();
        final String resource = "org/apache/ibatis/builder/MapperConfig.xml";
        final Reader reader = Resources.getResourceAsReader(resource);
        sqlMapper = new SqlSessionFactoryBuilder().build(reader);
    }

    @Test
    public void testSameLocalSession() {
        AuthorMapper authorMapper = sqlMapper.openSession(true)
                .getMapper(AuthorMapper.class);
        assert authorMapper instanceof Proxy;
        Configuration configuration = sqlMapper.getConfiguration();
//        assert !configuration.isCacheEnabled();
        Author author = authorMapper.selectAuthor(1);
        // 查询cache
        Author author2 = authorMapper.selectAuthor(1);
        System.out.println("一级缓存未命中");
        Author author3 = authorMapper.selectAuthor(2);
        System.out.println(author);
    }

    @Test
    public void testCache() {
        // 自动提交事务
        SqlSession sqlSession = sqlMapper.openSession(true);

        AuthorMapper mapper = sqlSession.getMapper(AuthorMapper.class);

        System.out.println(mapper.selectAuthor(101));
        mapper.selectAuthor(101);
        mapper.selectAuthor(101);

        sqlSession.close();
    }

    /**
     * 增加了对数据库的修改操作，验证在一次数据库会话中，如果对数据库发生了修改操作，一级缓存是否会失效。
     */
    @Test
    public void testLocalCache() {
        // 自动提交事务
        SqlSession sqlSession = sqlMapper.openSession(false);

        AuthorMapper mapper = sqlSession.getMapper(AuthorMapper.class);

        System.out.println(mapper.selectAuthor(101));
        System.out.println("--before insert--");
        mapper.selectAuthor(101);
        Author author = new Author();
        author.setId(2);
        author.setUsername("hahaha");
        author.setPassword("hhh");
        author.setEmail("sss@gmail.com");
        author.setBio("121223");
        author.setFavouriteSection(Section.NEWS);
//        mapper.insertAuthor(author);
        mapper.deleteAuthor(3);
        System.out.println("--after insert--");
        mapper.selectAuthor(101); // insert delete update 会使SqlSession一级缓存失效

        sqlSession.close();
    }

    /**
     * 测试二级缓存
     */
    @Test
    public void test2Cache() {
        SqlSession sqlSession = sqlMapper.openSession(true);
        SqlSession sqlSession2 = sqlMapper.openSession(true);

        AuthorMapper authorMapper1 = sqlSession.getMapper(AuthorMapper.class);
        AuthorMapper authorMapper2 = sqlSession2.getMapper(AuthorMapper.class);
        System.out.println("--1 select--");
        Author author = authorMapper1.selectAuthor(101);
        author.setUsername("北京欢迎你！");
        System.out.println("---1 update---");
        // 清空缓存
        authorMapper1.updateAuthor(author);
        // authorMapper1.deleteAuthor(101);
        // 手动提交事务
        // sqlSession.close();
        sqlSession.commit();
        System.out.println("-------2 select------");
        System.out.println(authorMapper2.selectAuthor(101));

    }

    @Test
    public void testPlaceholder() throws SQLException {
        SqlSession sqlSession = sqlMapper.openSession();
        AuthorMapper authorMapper = sqlSession.getMapper(AuthorMapper.class);
        List<Author> authors = authorMapper.selectAuthorWithInlineParams2(101);
        System.out.println(authors);

    }

}
