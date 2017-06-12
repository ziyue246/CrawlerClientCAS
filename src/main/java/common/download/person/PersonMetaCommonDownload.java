package common.download.person;

import java.util.ArrayList;
import java.util.List;

import common.bean.PersonData;
import common.bean.HtmlInfo;
import common.download.DataThreadControl;
import common.download.GenericMetaCommonDownload;
import common.rmi.packet.SearchKey;
import common.system.Systemconfig;
import common.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 下载元数据
 * 
 * @author grs
 */
public class PersonMetaCommonDownload extends GenericMetaCommonDownload<PersonData> implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(PersonMetaCommonDownload.class);

	public PersonMetaCommonDownload(SearchKey key) {
		super(key);
	}

	public void process() {
		List<PersonData> alllist = new ArrayList<PersonData>();
		List<PersonData> list = new ArrayList<PersonData>();
		String url = getRealUrl(siteinfo, gloaburl);
		int page = getRealPage(siteinfo);
		String keyword = key.getKey();
		map.put(keyword, 1);
		String nexturl = url;
		DataThreadControl dtc = new DataThreadControl(siteFlag, keyword);
		HtmlInfo html = htmlInfo("META");
		int totalCount = 0;
		while (nexturl != null && !nexturl.equals("")) {
			list.clear();

			html.setOrignUrl(nexturl);

			try {
				http.getContent(html);
				// html.setContent(common.util.StringUtil.getContent("filedown/META/baidu/37b30f2108ed06501ad6a769ca8cedc8.htm"));

				nexturl = xpath.templateListPage(list, html, map.get(keyword), keyword, nexturl, key.getRole() + "");

				if (list.size() == 0) {
					LOGGER.info(url + "元数据页面解析为空！！");
					break;
				}
				LOGGER.info(url + "元数据页面解析完成。");
				totalCount += list.size();
				Systemconfig.dbService.filterDuplication(list);
				if (list.size() == 0)
					break;
				alllist.addAll(list);

				map.put(keyword, map.get(keyword) + 1);
				if (map.get(keyword) > page)
					break;
				url = nexturl;
				if (nexturl != null)
					TimeUtil.rest(siteinfo.getDownInterval());

			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
		dtc.process(alllist, siteinfo.getDownInterval(),null, key);
	}
}