package net.simpleframework.module.news.web.page.mgr2;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.common.web.page.AbstractMgrTPage;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.module.news.web.page.NewsCategoryHandle;
import net.simpleframework.module.news.web.page.NewsFormTPage;
import net.simpleframework.module.news.web.page.NewsListTbl;
import net.simpleframework.module.news.web.page.NewsMgrActions;
import net.simpleframework.module.news.web.page.NewsMgrActions.StatusDescPage;
import net.simpleframework.module.news.web.page.NewsUtils;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.Icon;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.category.CategoryBean;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.TablePagerUtils;
import net.simpleframework.mvc.component.ui.pager.db.NavigationTitle;
import net.simpleframework.mvc.component.ui.tree.TreeNode;

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
		pp.addImportCSS(NewsFormTPage.class, "/news.css");

		// 导航树
		addComponentBean(pp, "NewsMgrTPage_tree", CategoryBean.class).setDraggable(pp.isLmanager())
				.setContainerId("idNewsMgrTPage_category").setHandlerClass(_NewsCategoryHandle.class);

		// 表格
		final TablePagerBean tablePager = (TablePagerBean) addComponentBean(pp, "NewsMgrTPage_tbl",
				TablePagerBean.class)
				.setResize(false)
				.setPageItems(30)
				.setPagerBarLayout(EPagerBarLayout.bottom)
				.setJsLoadedCallback(
						"$('idNewsMgrTPage_tbl').previous().innerHTML = $('idNewsMgrTPage_nav').innerHTML;")
				.setContainerId("idNewsMgrTPage_tbl").setHandlerClass(NewsListMgr2Tbl.class);
		tablePager.addColumn(TablePagerColumn.ICON()).addColumn(NewsListTbl.TC_TOPIC())
				.addColumn(NewsListTbl.TC_VIEWS()).addColumn(NewsListTbl.TC_COMMENTS())
				.addColumn(NewsListTbl.TC_CREATEDATE()).addColumn(TablePagerColumn.OPE(70));

		// edit/delete/status
		NewsMgrActions.addMgrComponentBean(pp, _NewsMgrActions.class, _StatusDescPage.class);
	}

	@Override
	public String getPageRole(final PageParameter pp) {
		return newsContext.getModule().getManagerRole();
	}

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		final ElementList btns = ElementList
				.of(NewsUtils.createAddNew(pp))
				.append(SpanElement.SPACE)
				.append(createStatusButton(EContentStatus.publish))
				.append(SpanElement.SPACE)
				.append(createStatusButton(EContentStatus.delete).setIconClass(Icon.trash))
				.append(SpanElement.SPACE)
				.append(
						new LinkButton($m("NewsMgrPage.13"))
								.setOnclick("$Actions['NewsMgrPage_advWindow']();"));
		final LinkButton preview = NewsUtils.createNewsPreview(pp);
		if (preview != null) {
			btns.append(SpanElement.SPACE).append(preview);
		}
		return btns;
	}

	static LinkButton createStatusButton(final EContentStatus status) {
		return TablePagerUtils.act_btn("NewsMgrTPage_tbl", "NewsMgrPage_status", status.toString(),
				"newsId", "op=" + status.name());
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='NewsMgrTPage'>");
		sb.append("	<table width='100%'><tr>");
		sb.append("  <td valign='top' class='ltree'><div id='idNewsMgrTPage_category'></div></td>");
		sb.append("  <td valign='top' class='rtbl'>");
		sb.append("    <div class='rtb'></div>");
		sb.append("    <div id='idNewsMgrTPage_tbl'></div></td>");
		sb.append(" </tr></table>");
		sb.append("</div>");
		return sb.toString();
	}

	public static class _NewsCategoryHandle extends NewsCategoryHandle {

		@Override
		protected void setJsClickCallback(final TreeNode tn, final NewsCategory category,
				final EContentStatus status) {
			final StringBuilder sb = new StringBuilder("$Actions['NewsMgrTPage_tbl']('categoryId=");
			if (category != null) {
				sb.append(category.getId());
			}
			sb.append("&status=");
			if (status != null) {
				sb.append(status.name());
			}
			tn.setJsClickCallback(sb.append("');").toString());
		}
	}

	public static class NewsListMgr2Tbl extends NewsListTbl {
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
	}

	public static class _NewsMgrActions extends NewsMgrActions {

		@Override
		protected JavascriptForward createTableRefresh() {
			return new JavascriptForward("$Actions['NewsMgrTPage_tbl']();");
		}
	}

	public static class _StatusDescPage extends StatusDescPage {

		@Override
		protected JavascriptForward createTableRefresh() {
			return new JavascriptForward("$Actions['NewsMgrTPage_tbl']();");
		}
	}
}
