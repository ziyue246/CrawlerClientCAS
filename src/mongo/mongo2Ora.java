package mongo;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import common.bean.WeixinData;
import net.sf.json.JSONObject;
import org.bson.Document;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class Mongo2Ora {

    static Logger logger = LoggerFactory.getLogger(Mongo2Ora.class);

    static final List<String> movedIds = new ArrayList<String>();
    static MongoClient client = new MongoClient("172.18.79.31:27017");
    static final MongoDatabase db = client.getDatabase("wechatdb");
    static MongoCollection coll = db.getCollection("wechat_article_info");


    public static void move() {


        FindIterable<Document> iter = coll.find();

        logger.info("start moving items from mongodb to oracle...");

        final Db<WeixinData> wxDb = new Db<WeixinData>();

        try {
            iter.forEach(new Block<Document>() {
                @Override
                public void apply(Document document) {
//                System.out.println(document);
                    logger.info("document: {}", document.get("_id"));


                    WeixinData wd = new WeixinData();
                    wd.setCategoryCode(0);
                    wd.setReadNum(0);
                    wd.setPraiseNum(0);
                    JSONObject jObjs = JSONObject.fromObject(document.toJson());
                    Iterator<String> iter = jObjs.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();

                        if (key.equals("brief")) {
                            wd.setBrief(jObjs.getString(key));
                        } else if (key.equals("title")) {
                            wd.setTitle(jObjs.getString(key));
                        } else if (key.equals("author")) {
                            wd.setAuthor(jObjs.getString(key));
                            wd.setSource(jObjs.getString(key));
                        } else if (key.equals("pubtime")) {
                            wd.setPubtime(jObjs.getString(key));
                            wd.setPubdate(parseTime(jObjs.getString(key)));
                        } else if (key.equals("url")) {
                            wd.setUrl(jObjs.getString(key));
                        } else if (key.equals("search_keyword")) {
                            wd.setSearchKey(jObjs.getString(key));
                        } else if (key.equals("insertTime")) {
                            wd.setInserttime(parseTime(jObjs.getString(key)));
                        } else if (key.equals("md5")) {
                            wd.setMd5(jObjs.getString(key));
                        } else if (key.equals("content")) {
                            wd.setContent(jObjs.getString(key));
                        } else if (key.equals("img_url")) {
                            wd.setImgUrl(jObjs.getString(key));
                        } else if (key.contains("read_num")) {
                            wd.setReadNum(jObjs.getInt(key) > wd.getReadNum() ? jObjs.getInt(key) : wd.getReadNum());
                        } else if (key.contains("like_num")) {
                            wd.setPraiseNum(jObjs.getInt(key) > wd.getPraiseNum() ? jObjs.getInt(key) : wd.getPraiseNum());
                        }


                    }


                    wxDb.saveData(wd);
                    if (wd.getReadNum() > 0) {
                        JSONObject obj = (JSONObject) jObjs.get("_id");

                        movedIds.add(obj.getString("$oid"));
                    }


                }
            });
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        logger.info("moved {} items.", movedIds.size());

    }

    public static void remove() {
        logger.info("start removing {} items from mongodb...", movedIds.size());
        for (String oid : movedIds) {
            Object removed = coll.findOneAndDelete(new Document("_id", new ObjectId(oid)));
            if (removed != null) {
                logger.info("item removed from mongodb: {}", oid);
            } else {
                logger.error("item not found in mongodb. {}", oid);
            }
        }
        logger.info("all removed.");
        movedIds.clear();
    }

    public static Date parseTime(String strTime) {

        if (strTime == null || strTime.equals("null")) {
            return null;
        }
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd hhmmss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        try {
            return sdf1.parse(strTime);
        } catch (ParseException e) {
            try {
                return sdf2.parse(strTime);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
        }
        return null;
    }

    public static void main(String[] args) {
        move();
        if (movedIds.size() > 0) remove();

    }
}