package base;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *@author lgh
 *@date:2016年5月4日上午11:21:58
 *@version:
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring.xml","classpath:spring-mvc.xml","classpath:spring-quartz.xml"})
public class BaseTest {

}
