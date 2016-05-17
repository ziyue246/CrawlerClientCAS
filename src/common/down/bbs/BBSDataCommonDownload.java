package common.down.bbs;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import common.bean.BBSData;
import common.bean.HtmlInfo;
import common.down.GenericDataCommonDownload;
import common.rmi.packet.SearchKey;
import common.service.mysql.BbsMysqlService;
import common.service.oracle.BbsOracleService;
import common.system.Systemconfig;
import common.system.UserAttr;
import common.system.UserManager;
import common.util.StringUtil;

/**
 * 下载详细页面
 * 
 * @author grs
 */
public class BBSDataCommonDownload extends GenericDataCommonDownload<BBSData> {

	public BBSDataCommonDownload(String siteFlag, BBSData vd, CountDownLatch endCount, SearchKey key) {
		super(siteFlag, vd, endCount, key);
	}

	@Override
	public void process() {
		String url = getRealUrl(data);
		if (siteFlag.startsWith("tieba")) {
			// if(!url.contains("pid")) return;
		}
		if (url == null)
			return;
		HtmlInfo html = htmlInfo("DATA");
		try {
			if (url != null && !url.equals("")) {
				html.setOrignUrl(url);
				http.getContent(html);

				Systemconfig.sysLog.log(data.getUrl() + "下载完成。。。");
				// html.setContent(StringUtil.getContent("E:/grs/开源工具/crawler(_client)_0.8.1/filedown/DATA/tieba_bbs_search/dcb64a74de7c2a750f5e5cfcf0d20697.htm",
				// siteinfo.getCharset()));
				if (html.getContent() == null||(html.getContent().contains("抱歉，您访问的贴子被隐藏")&&html.getContent().contains("贴吧404"))) {
					return;
				}
				// 解析数据
				url = xpath.templateContentPage(data, html);

				Systemconfig.sysLog.log(data.getTitle() + "解析完成。。。");
				Systemconfig.dbService.saveData(data);
				synchronized (key) {
					key.savedCountIncrease();
				}
				Systemconfig.sysLog.log(data.getTitle() + "保存完成。。。");
			}
		} catch (Exception e) {
			Systemconfig.sysLog.log("采集出现异常" + data.getUrl(), e);
		} finally {
			if (count != null)
				count.countDown();
		}
	}

}
