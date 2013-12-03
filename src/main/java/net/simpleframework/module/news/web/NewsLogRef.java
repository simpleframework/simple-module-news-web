package net.simpleframework.module.news.web;

import java.io.File;

import net.simpleframework.ado.bean.IIdBeanAware;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.module.log.LogRef;
import net.simpleframework.module.log.web.page.DownloadLogPage;
import net.simpleframework.module.log.web.page.EntityUpdateLogPage;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.News;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.window.WindowBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsLogRef extends LogRef implements INewsContextAware {

	public void addLogComponent(final PageParameter pp) {
		pp.addComponentBean("NewsMgrPage_status_logPage", AjaxRequestBean.class).setUrlForward(
				AbstractMVCPage.url(StatusLogViewPage.class));
		pp.addComponentBean("NewsMgrPage_status_logWindow", WindowBean.class)
				.setContentRef("NewsMgrPage_status_logPage").setHeight(540).setWidth(864);
	}

	@Override
	public void logDownload(final Object beanId, final String topic, final File oFile) {
		super.logDownload(beanId, topic, oFile);

		// 更新计数
		final IAttachmentService<Attachment> service = context.getAttachmentService();
		final Attachment attachment = service.getBean(beanId);
		if (attachment != null) {
			attachment.setDownloads(getDownloadLogService().countLog(beanId));
			service.update(new String[] { "downloads" }, attachment);
		}
	}

	public static class StatusLogViewPage extends EntityUpdateLogPage {

		@Override
		protected News getBean(final PageParameter pp) {
			return getCacheBean(pp, context.getNewsService(), getBeanIdParameter());
		}

		@Override
		public String getBeanIdParameter() {
			return "newsId";
		}
	}

	public static class NewsDownloadLogPage extends DownloadLogPage implements INewsContextAware {

		@Override
		protected IIdBeanAware getBean(final PageParameter pp) {
			return context.getAttachmentService().getBean(pp.getParameter(getBeanIdParameter()));
		}
	}
}