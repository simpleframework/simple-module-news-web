package net.simpleframework.module.news.web.page.mgr2;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.common.coll.KVMap;
import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.common.web.page.AbstractMgrTPage;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.module.news.web.page.NewsCategoryHandle;
import net.simpleframework.module.news.web.page.NewsFormTPage;
import net.simpleframework.module.news.web.page.NewsListTbl;
import net.simpleframework.module.news.web.page.NewsMgrActions;
import net.simpleframework.module.news.web.page.NewsMgrActions.StatusDescPage;
import net.simpleframework.module.news.web.page.NewsUtils;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.AbstractElement;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.category.CategoryBean;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
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
		pp.addImportCSS(NewsFormTPage.class, "/news_mgr2.css");

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
				.setContainerId("idNewsMgrTPage_tbl").setHandlerClass(_NewsListTbl.class);
		tablePager
				.addColumn(TablePagerColumn.ICON())
				.addColumn(new TablePagerColumn("topic", $m("NewsMgrPage.1")))
				.addColumn(
						new TablePagerColumn("stat", $m("NewsMgrPage.2") + "/" + $m("NewsMgrPage.3"), 90)
								.setTextAlign(ETextAlign.center).setFilterSort(false))
				.addColumn(TablePagerColumn.DATE("createDate", $m("NewsMgrPage.4")))
				.addColumn(TablePagerColumn.OPE(70));

		// edit
		addAjaxRequest(pp, "NewsMgrPage_edit").setHandlerMethod("doEdit").setHandlerClass(
				_NewsMgrActions.class);
		// delete
		addAjaxRequest(pp, "NewsMgrPage_delete").setConfirmMessage($m("NewsMgrPage.11"))
				.setHandlerMethod("doDelete").setHandlerClass(_NewsMgrActions.class);
		// status
		addAjaxRequest(pp, "NewsMgrPage_status").setHandlerMethod("doStatus").setHandlerClass(
				_NewsMgrActions.class);

		// status window
		addAjaxRequest(pp, "NewsMgrPage_statusPage", _StatusDescPage.class);
		addWindowBean(pp, "NewsMgrPage_statusWindow").setContentRef("NewsMgrPage_statusPage")
				.setWidth(420).setHeight(240);
	}

	LinkButton createStatusButton(final EContentStatus status) {
		return TablePagerUtils.act_btn("NewsMgrTPage_tbl", "NewsMgrPage_status", status.toString(),
				"newsId", "op=" + status.name());
	}

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		final ElementList btns = ElementList.of(NewsUtils.createAddNew(pp), SpanElement.SPACE);

		final EContentStatus status = pp.getEnumParameter(EContentStatus.class, "status");
		if (status != EContentStatus.delete) {
			btns.append(createStatusButton(EContentStatus.publish))
					.append(createStatusButton(EContentStatus.lock)).append(SpanElement.SPACE);
		}
		return btns;
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
			String params = "categoryId=";
			if (category != null) {
				params += category.getId();
			}
			params += "&status=";
			if (status != null) {
				params += status.name();
			}
			tn.setJsClickCallback("$Actions['NewsMgrTPage_tbl']('" + params + "');");
		}
	}

	public static class _NewsListTbl extends NewsListTbl {

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
					.add("createDate", news.getCreateDate())
					.add(TablePagerColumn.OPE, toOpeHTML(cp, news));
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
