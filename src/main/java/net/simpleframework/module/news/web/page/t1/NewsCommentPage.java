package net.simpleframework.module.news.web.page.t1;

import static net.simpleframework.common.I18n.$m;

import java.util.Date;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.common.web.html.HtmlUtils;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.news.INewsCommentService;
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
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ext.comments.CommentUtils;
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
	protected void onForward(final PageParameter pp) {
		super.onForward(pp);

		final TablePagerBean tablePager = addTablePagerBean(pp, "NewsCommentPage_tbl",
				NewsCommentTbl.class);
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

		// allowComments
		addAjaxRequest(pp, "NewsCommentPage_allowComments").setHandleMethod("doAllowComments");

		// delete
		addDeleteAjaxRequest(pp, "NewsCommentPage_delete");
	}

	public IForward doAllowComments(final ComponentParameter cp) {
		final News news = NewsViewTPage.getNews(cp);
		news.setAllowComments(cp.getBoolParameter("val"));
		context.getNewsService().update(new String[] { "allowComments" }, news);
		return new JavascriptForward("$('nc_allowComments').checked=").append(news.isAllowComments())
				.append(";");
	}

	@Transaction(context = INewsContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		if (ids != null) {
			final INewsCommentService cService = context.getCommentService();
			cService.delete(ids);
			final News news = NewsViewTPage.getNews(cp);
			news.setComments(cService.queryByContent(news).getCount());
			news.setLastCommentDate(new Date());
			context.getNewsService().update(new String[] { "comments", "lastCommentDate" }, news);
		}
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
				new Checkbox("nc_allowComments", $m("NewsForm.8")).setChecked(news.isAllowComments())
						.setOnchange(
								"$Actions['NewsCommentPage_allowComments']('newsId=" + news.getId()
										+ "&val=' + this.checked);"));
	}

	public static class NewsCommentTbl extends AbstractDbTablePagerHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final News news = NewsViewTPage.getNews(cp);
			cp.addFormParameter("newsId", news.getId());
			return context.getCommentService().queryByContent(news);
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final NewsComment comment = (NewsComment) dataObject;
			final KVMap kv = new KVMap();
			kv.add("content",
					HtmlUtils.truncateHtml(CommentUtils.replace(comment.getContent(), false), 100));
			kv.add("userId", cp.getUser(comment.getUserId()));
			kv.add("createDate", comment.getCreateDate());
			kv.add(
					TablePagerColumn.OPE,
					ButtonElement.deleteBtn().setOnclick(
							"$Actions['NewsCommentPage_delete']('newsId=" + comment.getContentId()
									+ "&id=" + comment.getId() + "');"));
			return kv;
		}
	}
}
