package common.service.oracle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import common.download.video.VideoDataCommonDownload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import common.bean.GovAffairData;
import common.bean.NewsData;
import common.system.Systemconfig;
import common.util.StringUtil;

public class GovAffairOracleService extends OracleService<GovAffairData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GovAffairOracleService.class);

    private static final String TABLE = "govaffair_data";


    private static final String jasql = "insert into " + TABLE + "(" +
        "title, " +
        "author," +
        "pubtime," +
        "source," +
        "url," +
        "inserttime," +
        "search_keyword," +
        "category_code," +
        "md5," +
        "content," +
        "brief," +
        "site_id," +
        "img_url) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	
    public void saveData(final GovAffairData vd) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try{
            this.jdbcTemplate.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement ps = con.prepareStatement(jasql, new String[]{"id"});
                    ps.setString(1, vd.getTitle());
                    ps.setString(2, vd.getAuthor());
                    ps.setTimestamp(3, vd.getPubdate() == null ? new Timestamp(0) : new Timestamp(vd.getPubdate().getTime()));
                    ps.setString(4, vd.getSource());
                    ps.setString(5, vd.getUrl());
                    ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
	                ps.setString(7, vd.getSearchKey());
	
	                ps.setInt(8, vd.getCategoryCode());//vd.getCategoryCode()
	                ps.setString(9, vd.getMd5());
	
	                ps.setString(10, vd.getContent() == null || vd.getContent().equals("") ? "no content." : vd.getContent());
	                ps.setString(11, vd.getBrief());
	                ps.setInt(12, vd.getSiteId());
	                ps.setString(13, vd.getImgUrl());
	             
	                return ps;
                }
            }, keyHolder);
        }
		catch(Exception e){
			LOGGER.info("插入异常！！！"+e.getMessage());
			return;
		}
        vd.setId(Integer.parseInt(StringUtil.extractMulti(keyHolder.getKeyList().get(0).toString(), "\\d")));
	}
}
