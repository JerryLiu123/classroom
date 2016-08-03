package com.classroom.wnn.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.classroom.wnn.service.RedisService;

@Service(value="redisService")
@SuppressWarnings("unchecked")
public class RedisServiceImpl implements RedisService {
    private static String redisCode = "utf-8";

    /**
     * @param key
     */
	public long del(final String... keys) {
        return redisTemplate.execute(new RedisCallback<Long>() { 
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                long result = 0;
                for (int i = 0; i < keys.length; i++) {
                    result = connection.del(keys[i].getBytes());
                }
                return result;
            }
        });
    }

    /**
     * @param key
     * @param value
     * @param liveTime
     */
    public void set(final byte[] key, final byte[] value, final long liveTime) {
        redisTemplate.execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                connection.set(key, value);
                if (liveTime > 0) {
                    connection.expire(key, liveTime);
                }
                return 1L;
            }
        });
    }

    /**
     * @param key
     * @param value
     * @param liveTime
     */
    public void set(String key, String value, long liveTime) {
        this.set(key.getBytes(), value.getBytes(), liveTime);
    }

    /**
     * @param key
     * @param value
     */
    public void set(String key, String value) {
        this.set(key, value, 0L);
    }

    /**
     * @param key
     * @param value
     */
    public void set(byte[] key, byte[] value) {
        this.set(key, value, 0L);
    }

    /**
     * @param key
     * @return
     */
    public String get(final String key) {
        return redisTemplate.execute(new RedisCallback<String>() {
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                	byte[] value = connection.get(key.getBytes());
                	if(value == null || value.length <=0){
                		return null;
                	}else{
                		return new String(value, redisCode);
                	}
                } catch (UnsupportedEncodingException e) {
                    //e.printStackTrace();
                    return null;
                }
            }
        });
    }
    
    /**
     * @param key
     * @return
     */
    public byte[] getByte(final String key) {
        return redisTemplate.execute(new RedisCallback<byte[]>() {
            public byte[] doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] value = connection.get(key.getBytes());
				if(value == null || value.length <=0){
					return null;
				}else{
					return value;
				}
            }
        });
    }

    /**
     * @param pattern
     * @return 
     * @return
     */
    public Object Setkeys(String pattern) {
        return redisTemplate.keys(pattern);

    }

    /**
     * @param key
     * @return
     */
    public boolean exists(final String key) {
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.exists(key.getBytes());
            }
        });
    }

    /**
     * @return
     */
    public String flushDB() {
        return redisTemplate.execute(new RedisCallback<String>() {
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                connection.flushDb();
                return "ok";
            }
        });
    }

    /**
     * @return
     */
    public long dbSize() {
        return redisTemplate.execute(new RedisCallback<Long>() {
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.dbSize();
            }
        });
    }

    /**
     * @return
     */
	public String ping() {
        return redisTemplate.execute(new RedisCallback<String>() {
            public String doInRedis(RedisConnection connection) throws DataAccessException {

                return connection.ping();
            }
        });
    }
	
	/**
	 * 根据key是否存在添加 如果 key存在则返回false，不存在则添加key后返回true
	 * @param key
	 * @param value
	 * @return
	 */
	public Boolean setValueForExist(final String key, final String value){
		return redisTemplate.execute(new RedisCallback<Boolean>() {
			public Boolean doInRedis(RedisConnection connection)
					throws DataAccessException {
				// TODO Auto-generated method stub
				//byte[] time = ObjectUtil.long2Bytes(System.currentTimeMillis());
				Boolean flag = connection.setNX(key.getBytes(), value.getBytes());
				return flag;
			}
		});
	}
	
	public String getSet(final String key, final String value) {
		// TODO Auto-generated method stub
		return redisTemplate.execute(new RedisCallback<String>() {
			public String doInRedis(RedisConnection connection)
					throws DataAccessException {
				// TODO Auto-generated method stub
				String cc = null;
				try {
					byte[] b = connection.getSet(key.getBytes(), value.getBytes());
					if(b != null && b.length > 0){
						cc = new String(b, redisCode);
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return cc;
			}
		});
	}
/*	
	public Boolean delAndSet(final String key, final String timeOld, final String timeNow) {
		// TODO Auto-generated method stub
		return redisTemplate.execute(new RedisCallback<Boolean>(){

			@Override
			public Boolean doInRedis(RedisConnection connection)
					throws DataAccessException {
				// TODO Auto-generated method stub
				boolean flag = false;
				try {
					connection.multi();
					byte[] b = connection.get(key.getBytes());
					if(b != null && b.length >0){
						if(new String(b, redisCode).equals(timeOld)){
							connection.set(key.getBytes(), timeNow.getBytes());
							flag = true;
						}else{
							flag = false;
						}
					}else{
						connection.set(key.getBytes(), timeNow.getBytes());
						flag = true;
					}
					List<Object> res = connection.exec();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					flag = false;
				}
				return flag;
			}});
	}*/

    public RedisServiceImpl() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Autowired
    private RedisTemplate<String, String> redisTemplate;


}
