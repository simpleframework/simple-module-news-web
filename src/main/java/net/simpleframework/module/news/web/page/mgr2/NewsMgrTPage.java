package net.simpleframework.module.news.web.page.mgr2;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.module.common.web.page.AbstractMgrTPage;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.web.page.NewsCategoryHandle;
import net.simpleframework.module.news.web.page.NewsFormTPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.component.ext.category.CategoryBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsMgrTPage extends AbstractMgrTPage implements INewsContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);
		pp.addImportCSS(NewsFormTPage.class, "/news_mgr2.css");

		// 导航树
		addComponentBean(pp, "NewsMgrTPage_tree", CategoryBean.class).setDraggable(pp.isLmanager())
				.setContainerId("idNewsMgrTPage_category").setHandlerClass(_NewsCategoryHandle.class);

		// 表格
		// final TablePagerBean tablePager = (TablePagerBean) addComponentBean(pp,
		// "DictMgrTPage_tbl",
		// TablePagerBean.class).setShowLineNo(true).setPageItems(30)
		// .setPagerBarLayout(EPagerBarLayout.top).setContainerId("idDictMgrTPage_tbl")
		// .setHandlerClass(_DictItemList.class);
		// tablePager
		// .addColumn(TablePagerColumn.ICON())
		// .addColumn(new TablePagerColumn("text", $m("DictMgrPage.1")))
		// .addColumn(new TablePagerColumn("codeNo", $m("DictMgrPage.2")))
		// .addColumn(
		// new TablePagerColumn("parentId", $m("DictMgrPage.8"),
		// 100).setFilterSort(false))
		// .addColumn(TablePagerColumn.OPE(70));

	}

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		return ElementList.of();
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='NewsMgrTPage'>");
		sb.append("	<table width='100%'><tr>");
		sb.append("  <td valign='top' class='ltree'><div id='idNewsMgrTPage_category'></div></td>");
		sb.append("  <td valign='top' class='rtbl'><div id='idNewsMgrTPage_tbl'></div></td>");
		sb.append(" </tr></table>");
		sb.append("</div>");
		return sb.toString();
	}

	public static class _NewsCategoryHandle extends NewsCategoryHandle {
	}
}
