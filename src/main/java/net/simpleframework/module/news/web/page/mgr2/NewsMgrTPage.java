package net.simpleframework.module.news.web.page.mgr2;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.common.coll.KVMap;
import net.simpleframework.module.common.web.page.AbstractMgrTPage;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.News;
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
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.category.CategoryBean;
import net.simpleframework.mvc.component.ui.pager.AbstractTablePagerSchema;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;

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
				TablePagerBean.class).setShowLineNo(true).setPageItems(30)
				.setPagerBarLayout(EPagerBarLayout.top).setContainerId("idNewsMgrTPage_tbl")
				.setHandlerClass(_NewsListTbl.class);
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

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		final ElementList btns = ElementList.of(NewsUtils.createAddNew(pp));
		return btns;
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

	public static class _NewsListTbl extends NewsListTbl {
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