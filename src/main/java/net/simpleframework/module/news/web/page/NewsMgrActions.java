package net.simpleframework.module.news.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.ArrayList;
import java.util.Date;

import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.common.web.page.AbstractDescPage;
import net.simpleframework.module.news.INewsContext;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.NewsLogRef.NewsUpdateLogPage;
import net.simpleframework.module.news.web.NewsUrlsFactory;
import net.simpleframework.module.news.web.page.t1.NewsCommentPage;
import net.simpleframework.module.news.web.page.t1.NewsFormBasePage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.JS;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.base.ajaxrequest.DefaultAjaxRequestHandler;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.mvc.template.t1.ext.CategoryTableLCTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsMgrActions extends DefaultAjaxRequestHandler implements INewsContextAware {

	public static void addMgrComponentBean(final PageParameter pp,
			final Class<? extends NewsMgrActions> mgrActionsClass,
			final Class<? extends StatusDescPage> statusDescClass) {
		// edit
		pp.addComponentBean("NewsMgrPage_edit", AjaxRequestBean.class).setHandlerMethod("doEdit")
				.setHandlerClass(mgrActionsClass);
		// delete
		pp.addComponentBean("NewsMgrPage_delete", AjaxRequestBean.class)
				.setConfirmMessage($m("NewsMgrPage.11")).setHandlerMethod("doDelete")
				.setHandlerClass(mgrActionsClass);
		// status
		pp.addComponentBean("NewsMgrPage_status", AjaxRequestBean.class).setHandlerMethod("doStatus")
				.setHandlerClass(mgrActionsClass);

		// status window
		pp.addComponentBean("NewsMgrPage_statusPage", AjaxRequestBean.class).setUrlForward(
				AbstractMVCPage.url(statusDescClass));
		pp.addComponentBean("NewsMgrPage_statusWindow", WindowBean.class)
				.setContentRef("NewsMgrPage_statusPage").setWidth(420).setHeight(240);

		// comment window
		pp.addComponentBean("NewsMgrPage_commentPage", AjaxRequestBean.class).setUrlForward(
				AbstractMVCPage.url(NewsCommentPage.class));
		pp.addComponentBean("NewsMgrPage_commentWindow", WindowBean.class)
				.setContentRef("NewsMgrPage_commentPage").setHeight(540).setWidth(864);

		// log window
		final IModuleRef ref = ((INewsWebContext) newsContext).getLogRef();
		if (ref != null) {
			pp.addComponentBean("NewsMgrPage_update_logPage", AjaxRequestBean.class).setUrlForward(
					AbstractMVCPage.url(NewsUpdateLogPage.class));
			pp.addComponentBean("NewsMgrPage_update_log", WindowBean.class)
					.setContentRef("NewsMgrPage_update_logPage").setHeight(540).setWidth(864);
		}
	}

	public IForward doEdit(final ComponentParameter cp) {
		final JavascriptForward js = new JavascriptForward();
		final News news = NewsUtils.getNews(cp);
		final EContentStatus status = news.getStatus();
		if (status == EContentStatus.edit) {
			final NewsUrlsFactory uFactory = ((INewsWebContext) newsContext).getUrlsFactory();
			js.append(JS.loc(uFactory.getUrl(cp, NewsFormBasePage.class, news)));
		} else {
			js.append("if (confirm('").append($m("NewsMgrPage.8", status))
					.append("')) { $Actions['NewsMgrPage_statusWindow']('op=")
					.append(EContentStatus.edit.name()).append("&newsId=").append(news.getId())
					.append("'); }");
		}
		return js;
	}

	@Transaction(context = INewsContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("newsId"));
		_newsService.delete(ids);
		return createTableRefresh();
	}

	public IForward doStatus(final ComponentParameter cp) {
		final JavascriptForward js = new JavascriptForward();
		final EContentStatus op = cp.getEnumParameter(EContentStatus.class, "op");
		final String newsId = cp.getParameter("newsId");
		final ArrayList<String> deletes = new ArrayList<String>();
		for (final String id : StringUtils.split(newsId, ";")) {
			final News news = _newsService.getBean(id);
			final EContentStatus status = news.getStatus();
			if (op == status && op != EContentStatus.delete) {
				js.append("alert('").append($m("NewsMgrPage.9", op)).append("');");
				return js;
			}
			if (status == EContentStatus.delete) {
				deletes.add(news.getId().toString());
			}
		}
		if (op == EContentStatus.delete && deletes.size() > 0) {
			js.append("$Actions['NewsMgrPage_delete']('newsId=")
					.append(StringUtils.join(deletes, ";")).append("')");
		} else {
			js.append("$Actions['NewsMgrPage_statusWindow']('op=").append(op.name())
					.append("&newsId=").append(newsId).append("');");
		}
		return js;
	}

	protected JavascriptForward createTableRefresh() {
		return CategoryTableLCTemplatePage.createTableRefresh();
	}

	public static class StatusDescPage extends AbstractDescPage {

		@Override
		@Transaction(context = INewsContext.class)
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			final EContentStatus op = cp.getEnumParameter(EContentStatus.class, "op");
			final String[] arr = StringUtils.split(cp.getParameter("newsId"), ";");
			for (final String id : arr) {
				final News news = _newsService.getBean(id);
				setLogDescription(cp, news);
				news.setStatus(op);
				_newsService.update(new String[] { "status" }, news);
			}

			final JavascriptForward js = super.onSave(cp);
			if (arr.length == 1 && op == EContentStatus.edit) {
				// final NewsUrlsFactory uFactory = ((INewsWebContext)
				// newsContext).getUrlsFactory();
				// js.append(JS.loc(uFactory.getUrl(cp, NewsFormBasePage.class,
				// news)));
			}
			return js.append(createTableRefresh().toString());
		}

		protected JavascriptForward createTableRefresh() {
			return CategoryTableLCTemplatePage.createTableRefresh();
		}

		@Override
		public String getTitle(final PageParameter pp) {
			final EContentStatus op = pp.getEnumParameter(EContentStatus.class, "op");
			return $m("StatusDescLogPage.0",
					op == EContentStatus.edit ? $m("NewsMgrPage.7") : Convert.toString(op));
		}

		@Override
		protected InputElement createTextarea(final PageParameter pp) {
			final EContentStatus op = pp.getEnumParameter(EContentStatus.class, "op");
			return super.createTextarea(pp).setText(
					$m("StatusDescLogPage.1",
							op == EContentStatus.edit ? $m("NewsMgrPage.7") : Convert.toString(op),
							Convert.toDateString(new Date()), pp.getLogin()));
		}
	}
}