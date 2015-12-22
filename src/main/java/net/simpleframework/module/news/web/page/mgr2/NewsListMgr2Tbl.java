package net.simpleframework.module.news.web.page.mgr2;

import java.util.Map;

import net.simpleframework.common.coll.KVMap;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.web.page.NewsListTbl;
import net.simpleframework.module.news.web.page.NewsUtils;
import net.simpleframework.mvc.common.element.AbstractElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.NavigationTitle;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsListMgr2Tbl extends NewsListTbl {
	@Override
	public String toTableHTML(final ComponentParameter cp) {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div id='idNewsMgrTPage_nav' style='display: none;'>");
		sb.append(NavigationTitle.toElement(cp, NewsUtils.getNewsCategory(cp),
				NewsUtils.createNavigationTitleCallback(cp, "NewsMgrTPage_tbl")));
		sb.append("</div>");
		sb.append(super.toTableHTML(cp));
		return sb.toString();
	}

	@Override
	protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
		final News news = (News) dataObject;
		final KVMap kv = new KVMap();

		final AbstractElement<?> img = createImageMark(cp, news);
		if (img != null) {
			kv.add(TablePagerColumn.ICON, img);
		}
		kv.add("topic", toTopicHTML(cp, news))
				.add("stat", news.getViews() + "/" + news.getComments())
				.add("createDate", news.getCreateDate()).add(TablePagerColumn.OPE, toOpeHTML(cp, news));
		return kv;
	}

	@Override
	protected String toOpeHTML(final ComponentParameter cp, final News news) {
		final StringBuilder sb = new StringBuilder();
		sb.append(createPublishBtn(cp, news));
		sb.append(AbstractTablePagerSchema.IMG_DOWNMENU);
		return sb.toString();
	}
}
