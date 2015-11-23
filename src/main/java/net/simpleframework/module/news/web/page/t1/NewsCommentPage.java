package net.simpleframework.module.news.web.page.t1;

import static net.simpleframework.common.I18n.$m;

import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.news.INewsContext;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.NewsComment;
import net.simpleframework.module.news.web.page.NewsViewTPage;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.Checkbox;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.template.lets.OneTableTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsCommentPage extends OneTableTemplatePage implements INewsContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		final TablePagerBean tablePager = addTablePagerBean(pp, "NewsCommentPage_tbl",
				NewsCommentTbl.class);
		tablePager
				.addColumn(
						new TablePagerColumn("content", $m("NewsCommentPage.0")).setNowrap(false)
								.setSort(false))
				.addColumn(
						new TablePagerColumn("userId", $m("NewsCommentPage.1"), 100).setFilter(false))
				.addColumn(TablePagerColumn.DATE("createDate", $m("NewsCommentPage.2")))
				.addColumn(TablePagerColumn.OPE(80));

		// allowComments
		addAjaxRequest(pp, "NewsCommentPage_allowComments").setHandlerMethod("doAllowComments");

		// delete
		addDeleteAjaxRequest(pp, "NewsCommentPage_delete");
	}

	public IForward doAllowComments(final ComponentParameter cp) {
		final News news = NewsViewTPage.getNews(cp);
		news.setAllowComments(cp.getBoolParameter("val"));
		_newsService.update(new String[] { "allowComments" }, news);
		return new JavascriptForward("$('nc_allowComments').checked=").append(news.isAllowComments())
				.append(";");
	}

	@Transaction(context = INewsContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		_newsCommentService.delete(ids);
		return new JavascriptForward("$Actions['NewsCommentPage_tbl']();");
	}

	@Override
	public String getTitle(final PageParameter pp) {
		return $m("NewsMgrPage.12") + "- " + NewsViewTPage.getNews(pp).getTopic();
	}

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		final News news = NewsViewTPage.getNews(pp);
		return ElementList.of(
				LinkButton.closeBtn(),
				SpanElement.SPACE,
				LinkButton.deleteBtn().setOnclick(
						"$Actions['NewsCommentPage_tbl'].doAct('NewsCommentPage_delete');"),
				SpanElement.SPACE,
				new Checkbox("nc_allowComments", $m("NewsFormTPage.8")).setChecked(
						news.isAllowComments()).setOnchange(
						"$Actions['NewsCommentPage_allowComments']('newsId=" + news.getId()
								+ "&val=' + this.checked);"));
	}

	@Override
	public String getPageRole(final PageParameter pp) {
		return newsContext.getModule().getManagerRole();
	}

	public static class NewsCommentTbl extends AbstractDbTablePagerHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final News news = NewsViewTPage.getNews(cp);
			cp.addFormParameter("newsId", news.getId());
			return _newsCommentService.queryComments(news);
		}

		protected ButtonElement createDelBtn(final NewsComment comment) {
			return ButtonElement.deleteBtn().setOnclick(
					"$Actions['NewsCommentPage_delete']('newsId=" + comment.getContentId() + "&id="
							+ comment.getId() + "');");
		}

		protected String getContent(final PageParameter pp, final NewsComment comment) {
			return StringUtils.substring(comment.doc().text(), 50);
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final NewsComment comment = (NewsComment) dataObject;
			final KVMap kv = new KVMap();
			kv.add("content", getContent(cp, comment));
			kv.add("userId", cp.getUser(comment.getUserId()));
			kv.add("createDate", comment.getCreateDate());
			kv.add(TablePagerColumn.OPE, createDelBtn(comment));
			return kv;
		}
	}
}
