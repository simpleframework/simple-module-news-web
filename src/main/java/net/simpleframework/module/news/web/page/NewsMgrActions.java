package net.simpleframework.module.news.web.page;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.common.content.AbstractContentBean.EContentStatus;
import net.simpleframework.module.common.web.page.AbstractDescPage;
import net.simpleframework.module.news.INewsContext;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.bean.News;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.NewsLogRef.NewsUpdateLogPage;
import net.simpleframework.module.news.web.page.t1.NewsCommentPage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.base.ajaxrequest.DefaultAjaxRequestHandler;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.mvc.template.AbstractTemplatePage;
import net.simpleframework.mvc.template.t1.ext.CategoryTableLCTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsMgrActions extends DefaultAjaxRequestHandler implements INewsContextAware {

	public static void addMgrComponentBean(final PageParameter pp,
			final Class<? extends NewsMgrActions> mgrActionsClass,
			final Class<? extends StatusDescPage> statusDescClass) {
		// delete
		pp.addComponentBean("NewsMgrPage_delete", AjaxRequestBean.class)
				.setConfirmMessage($m("NewsMgrPage.11")).setHandlerMethod("doDelete")
				.setHandlerClass(mgrActionsClass);

		// status && window
		addStatusWindow(pp, mgrActionsClass, statusDescClass);

		// comment window
		AjaxRequestBean ajaxRequest = pp
				.addComponentBean("NewsMgrPage_commentPage", AjaxRequestBean.class)
				.setUrlForward(AbstractMVCPage.url(NewsCommentPage.class));
		pp.addComponentBean("NewsMgrPage_commentWindow", WindowBean.class)
				.setContentRef(ajaxRequest.getName()).setHeight(540).setWidth(864);

		// adv window
		ajaxRequest = pp.addComponentBean("NewsMgrPage_advPage", AjaxRequestBean.class)
				.setUrlForward(AbstractMVCPage.url(NewsAdvPage.class));
		pp.addComponentBean("NewsMgrPage_advWindow", WindowBean.class)
				.setContentRef(ajaxRequest.getName()).setTitle($m("NewsMgrPage.13")).setHeight(280)
				.setWidth(420);

		// 推荐
		ajaxRequest = pp.addComponentBean("NewsMgrPage_recommendationPage", AjaxRequestBean.class)
				.setUrlForward(AbstractMVCPage.url(RecommendMgrPage.class));
		pp.addComponentBean("NewsMgrPage_recommendation", WindowBean.class)
				.setContentRef(ajaxRequest.getName()).setHeight(500).setWidth(740)
				.setTitle($m("AbstractContentBean.2"));

		// log window
		final IModuleRef ref = ((INewsWebContext) newsContext).getLogRef();
		if (ref != null) {
			ajaxRequest = pp.addComponentBean("NewsMgrPage_update_logPage", AjaxRequestBean.class)
					.setUrlForward(AbstractMVCPage.url(NewsUpdateLogPage.class));
			pp.addComponentBean("NewsMgrPage_update_log", WindowBean.class)
					.setContentRef(ajaxRequest.getName()).setHeight(540).setWidth(864);
		}
	}

	public static void addStatusWindow(final PageParameter pp,
			final Class<? extends NewsMgrActions> mgrActionsClass,
			final Class<? extends StatusDescPage> statusDescClass) {
		pp.addComponentBean("NewsMgrPage_status", AjaxRequestBean.class).setHandlerMethod("doStatus")
				.setHandlerClass(mgrActionsClass);

		final AjaxRequestBean ajaxRequest = pp
				.addComponentBean("NewsMgrPage_statusPage", AjaxRequestBean.class)
				.setUrlForward(AbstractMVCPage.url(statusDescClass));
		pp.addComponentBean("NewsMgrPage_statusWindow", WindowBean.class)
				.setContentRef(ajaxRequest.getName()).setWidth(420).setHeight(240);
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
			js.append("$Actions['NewsMgrPage_delete']('newsId=").append(StringUtils.join(deletes, ";"))
					.append("')");
		} else {
			js.append("$Actions['NewsMgrPage_statusWindow']('op=").append(op.name()).append("&newsId=")
					.append(newsId).append("');");
		}
		return js;
	}

	protected JavascriptForward createTableRefresh() {
		return CategoryTableLCTemplatePage.createTableRefresh();
	}

	public static class StatusDescPage extends AbstractDescPage {

		@Transaction(context = INewsContext.class)
		@Override
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			final EContentStatus op = cp.getEnumParameter(EContentStatus.class, "op");
			final String[] arr = StringUtils.split(cp.getParameter("newsId"), ";");
			News news = null;
			for (final String id : arr) {
				news = _newsService.getBean(id);
				setLogDescription(cp, news);
				news.setStatus(op);
				_newsService.update(new String[] { "status" }, news);
			}

			final JavascriptForward js = super.onSave(cp).append(toSaveJavascript(cp));
			return js;
		}

		protected String toSaveJavascript(final PageParameter pp) throws Exception {
			return CategoryTableLCTemplatePage.createTableRefresh().toString();
		}

		@Override
		public String getTitle(final PageParameter pp) {
			final EContentStatus op = pp.getEnumParameter(EContentStatus.class, "op");
			return $m("StatusDescLogPage.0",
					op == EContentStatus.edit ? $m("NewsMgrPage.7") : Convert.toString(op));
		}

		@Override
		public String toTableRowsString(final PageParameter pp) {
			final StringBuilder sb = new StringBuilder();
			final EContentStatus op = pp.getEnumParameter(EContentStatus.class, "op");
			if (op != null) {
				sb.append(InputElement.hidden("op").setVal(op.name()));
			}
			final String newsId = pp.getParameter("newsId");
			if (StringUtils.hasText(newsId)) {
				sb.append(InputElement.hidden("newsId").setVal(newsId));
			}
			sb.append(super.toTableRowsString(pp));
			return sb.toString();
		}

		@Override
		protected InputElement createTextarea(final PageParameter pp) {
			final EContentStatus op = pp.getEnumParameter(EContentStatus.class, "op");
			return super.createTextarea(pp).setText($m("StatusDescLogPage.1",
					op == EContentStatus.edit ? $m("NewsMgrPage.7") : Convert.toString(op),
					Convert.toDateTimeString(new Date()), pp.getLogin()));
		}
	}

	public static class NewsAdvPage extends AbstractTemplatePage implements INewsContextAware {

		@Override
		protected void onForward(final PageParameter pp) throws Exception {
			super.onForward(pp);

			addAjaxRequest(pp, "NewsAdvPage_reIndex").setConfirmMessage($m("NewsAdvPage.2"))
					.setHandlerMethod("doIndex");
		}

		@Override
		public Map<String, Object> createVariables(final PageParameter pp) {
			return ((KVMap) super.createVariables(pp)).add("LinkButton", LinkButton.class);
		}

		public IForward doIndex(final ComponentParameter cp) {
			_newsService.getLuceneService().rebuildIndex();
			return JavascriptForward.alert($m("NewsAdvPage.3"));
		}

		@Override
		protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
				final String currentVariable) throws IOException {
			final StringBuilder sb = new StringBuilder();
			sb.append("<div class='NewsAdvPage'>");
			sb.append(" <div class='cc1'><p>#(NewsAdvPage.1)</p>");
			sb.append("  <div>").append(LinkButton.corner($m("NewsAdvPage.0"))
					.setOnclick("$Actions['NewsAdvPage_reIndex']();"));
			sb.append("  </div>");
			sb.append(" </div>");
			sb.append(" <div class='bc'>").append(LinkButton.closeBtn().corner()).append("</div>");
			sb.append("</div>");
			return sb.toString();
		}
	}
}