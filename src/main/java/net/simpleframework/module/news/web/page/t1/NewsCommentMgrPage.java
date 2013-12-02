package net.simpleframework.module.news.web.page.t1;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.NewsComment;
import net.simpleframework.module.news.web.page.NewsForm;
import net.simpleframework.module.news.web.page.t1.NewsCommentPage.NewsCommentTbl;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.TabButtons;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.template.t1.T1ResizedTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@PageMapping(url = "/news/comment/mgr")
public class NewsCommentMgrPage extends T1ResizedTemplatePage implements INewsContextAware {

	@Override
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		pp.addImportCSS(NewsForm.class, "/news.css");

		final TablePagerBean tablePager = (TablePagerBean) addTablePagerBean(pp,
				"NewsCommentMgrPage_tbl").setPageItems(30).setPagerBarLayout(EPagerBarLayout.bottom)
				.setContainerId("tbl_" + hashId).setHandleClass(NewsCommentMgrTbl.class);
		tablePager
				.addColumn(
						new TablePagerColumn("content", $m("NewsCommentPage.0")).setNowrap(false)
								.setTextAlign(ETextAlign.left).setSort(false))
				.addColumn(
						new TablePagerColumn("userId", $m("NewsCommentPage.1"), 100).setFilter(false))
				.addColumn(
						new TablePagerColumn("createDate", $m("NewsCommentPage.2"), 120)
								.setPropertyClass(Date.class))
				.addColumn(TablePagerColumn.OPE().setWidth(80));

		// delete
		addDeleteAjaxRequest(pp, "NewsCommentMgrPage_delete");
	}

	public IForward doDelete(final ComponentParameter cp) {
		return null;
	}

	@Override
	public String getRole(final PageParameter pp) {
		return context.getManagerRole();
	}

	@Override
	protected TabButtons getTabButtons(final PageParameter pp) {
		return singleton(NewsMgrPage.class).getTabButtons(pp);
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div align='center' class='NewsCommentMgrPage'>");
		sb.append("  <div id='tbl_").append(hashId).append("'></div>");
		sb.append("</div>");
		return sb.toString();
	}

	public static class NewsCommentMgrTbl extends NewsCommentTbl {

		@Override
		protected ButtonElement createDelBtn(final NewsComment comment) {
			return ButtonElement.deleteBtn().setOnclick(
					"$Actions['NewsCommentMgrPage_delete']('id=" + comment.getId() + "');");
		}

		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			return context.getCommentService().queryAll();
		}
	}
}