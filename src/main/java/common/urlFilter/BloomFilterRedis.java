package common.urlFilter;

import common.system.Systemconfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.List;
import java.util.Set;

/**
 * Created by guanxiaoda on 2017/6/12.
 */
public class BloomFilterRedis {
    private static Logger LOGGER = LoggerFactory.getLogger(BloomFilterRedis.class);

    private BloomFilter<String> bloomFilter;
    private Set<HostAndPort> hostAndPortSet;
    private JedisCluster jedisCluster;

    public void init() {
        JedisCluster redis = new JedisCluster(hostAndPortSet);
        bloomFilter.bind(redis, "redis-bloom");
        LOGGER.info("redis bloom filter bind succeed.");
        try {
            if (redis.get("init_status") == null||redis.get("init_status").equals("0")) {
                redis.set("init_status", "1");
                List<String> allMD5 = Systemconfig.dbService.getAllMd5(Systemconfig.table);
                LOGGER.info("get {} items from database.", allMD5.size());
                LOGGER.info("adding items to redis bloom filter...");
                int num = 0;
                for (String md5 : allMD5) {
                    bloomFilter.add(md5);
                    if (num++ % 1000 == 0) {
                        LOGGER.info("{} item added to redis bloom filter.");
                    }
                }
                LOGGER.info("redis bloom filter init ok. total: [{}] items ");
                redis.set("init_status","2");
            } else if (redis.get("init_status").equals("1")) {
                LOGGER.info("other client is initializing redis bloom filter, program will exit. ");
                System.exit(0);
            } else {
                LOGGER.info("redis bloom filter available[ok].");
            }
        }catch (Exception e){
            redis.set("init_status","0");
        }
    }

    public boolean contains(String element) {
        return bloomFilter.contains(element);
    }

    public void add(String element) {
        bloomFilter.add(element);
    }


    public BloomFilter<String> getBloomFilter() {
        return bloomFilter;
    }

    public void setBloomFilter(BloomFilter<String> bloomFilter) {
        this.bloomFilter = bloomFilter;
    }

    public Set<HostAndPort> getHostAndPortSet() {
        return hostAndPortSet;
    }

    public void setHostAndPortSet(Set<HostAndPort> hostAndPortSet) {
        this.hostAndPortSet = hostAndPortSet;
    }

}