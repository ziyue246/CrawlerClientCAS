package common.download.report;

import java.util.concurrent.CountDownLatch;

import common.bean.HtmlInfo;
import common.bean.ReportData;
import common.download.GenericDataCommonDownload;
import common.rmi.packet.SearchKey;
import common.system.Systemconfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 下载详细页面
 * @author grs
 */
public class ReportDataCommonDownload extends GenericDataCommonDownload<ReportData> implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReportDataCommonDownload.class);


	public ReportDataCommonDownload(String siteFlag, ReportData data, CountDownLatch endCount, SearchKey key) {
		super(siteFlag, data, endCount, key);
	}
	
	public void process() {
		String url = getRealUrl(data);
		if(url==null) return;
		//检测是否需要代理，未来版本改进
		siteinfo.setAgent(false);
		HtmlInfo html = htmlInfo("DATA");
		specialHtmlInfo(html);
		try {
			if (url != null && !url.equals("")) {
				html.setOrignUrl(url);
				
				http.getContent(html);
				
				if(html.getContent()==null) {
					return;
				}
				//解析数据
				xpath.templateContentPage(data, html);
				
				LOGGER.info(data.getTitle() + "解析完成。。。");
				Systemconfig.dbService.saveData(data);
				LOGGER.info(data.getTitle() + "保存完成。。。");
			}
		} catch (Exception e) {
			LOGGER.info("采集出现异常"+url, e);
		} finally {
			if(count != null)
				count.countDown();
		}
	}
	
	@Override
	protected void specialHtmlInfo(HtmlInfo html) {
		html.setFileType(".pdf");
	}
}