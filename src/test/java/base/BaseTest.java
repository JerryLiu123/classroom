package base;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.classroom.wnn.dao.BiZoneInfoMapper;
import com.classroom.wnn.model.BiZoneInfo;

/**
 *@author lgh
 *@date:2016年5月4日上午11:21:58
 *@version:
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring.xml","classpath:spring-mybatis.xml","classpath:spring-quartz.xml","classpath:spring-mvc.xml"})
public class BaseTest {

//	@Autowired
//	private BiZoneInfoMapper biZoneInfoMapper;
	
/*	@Test
	public void testName() throws Exception {
		BiZoneInfo biZoneInfo = new BiZoneInfo();
		biZoneInfo.setvFileid(11111);
		biZoneInfo.setzAvailable(11111);
		biZoneInfo.setzFile("testFile1");
		biZoneInfo.setzHdfsfile("testHdfsFile2");
		biZoneInfo.setzIsdel(11111);
		//biZoneInfoMapper.insert(biZoneInfo);
		System.out.println("------------");
		
	}*/
}
