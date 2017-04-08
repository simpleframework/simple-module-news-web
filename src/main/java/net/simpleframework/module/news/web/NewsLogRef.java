package net.simpleframework.module.news.web;

import java.io.IOException;

import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.ctx.service.ado.db.IDbBeanService;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.module.log.LogRef;
import net.simpleframework.module.log.web.hdl.AbstractAttachmentLogHandler;
import net.simpleframework.module.log.web.page.DownloadLogPage;
import net.simpleframework.module.log.web.page.EntityUpdateLogPage;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.INewsService;
import net.simpleframework.module.news.bean.News;
import net.simpleframework.module.news.bean.NewsAttachment;
import net.simpleframework.module.news.web.page.NewsFormTPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.AbstractElement;
import net.simpleframework.mvc.common.element.ImageElement;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.ui.swfupload.SwfUploadBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsLogRef extends LogRef implements INewsContextAware {

	@Override
	public void logDownload(final Object beanId, final long length, final String filetype,
			final String topic) {
		super.logDownload(beanId, length, filetype, topic);

		// 更新计数
		final IAttachmentService<NewsAttachment> service = newsContext.getAttachmentService();
		final NewsAttachment attachment = service.getBean(beanId);
		if (attachment != null) {
			attachment.setDownloads(getDownloadLogService().clog(beanId));
			service.update(new String[] { "downloads" }, attachment);
		}
	}

	public static class NewsUpdateLogPage extends EntityUpdateLogPage {

		@Override
		protected IDbBeanService<?> getBeanService() {
			return _newsService;
		}

		@Override
		public String getBeanIdParameter(final PageParameter pp) {
			return "newsId";
		}
	}

	public static class NewsDownloadLogPage extends DownloadLogPage implements INewsContextAware {

		@Override
		protected IDbBeanService<?> getBeanService() {
			return newsContext.getAttachmentService();
		}
	}

	public static class NewsInsertAttachmentAction extends NewsAttachmentAction {

		@Override
		public AbstractElement<?> getDownloadLinkElement(final ComponentParameter cp,
				final AttachmentFile attachmentFile, final String id) throws IOException {
			if (Convert.toBool(cp.getParameter(NewsFormTPage.OPT_VIEWER))) {
				final ImageElement iElement = createImageViewer(cp, attachmentFile, id);
				if (iElement != null) {
					return iElement;
				}
			}
			return new LinkElement(attachmentFile.getTopic())
					.setOnclick("$Actions['NewsViewTPage_download']('id=" + id + "');");
		}

		@Override
		protected int getAttachtype(final ComponentParameter cp) {
			return 1;
		}
	}

	public static class NewsAttachmentAction
			extends AbstractAttachmentLogHandler<NewsAttachment, News> {
		@Override
		protected IAttachmentService<NewsAttachment> getAttachmentService() {
			return _newsAttachService;
		}

		@Override
		protected INewsService getOwnerService() {
			return _newsService;
		}

		@Override
		protected String getOwnerIdParameterKey() {
			return "newsId";
		}

		@Override
		protected int getAttachtype(final ComponentParameter cp) {
			return 0;
		}

		@Override
		public void setSwfUploadBean(final ComponentParameter cp, final SwfUploadBean swfUpload) {
			super.setSwfUploadBean(cp, swfUpload);
			final String attachmentMaxSize = ((INewsWebContext) newsContext).getAttachmentMaxSize();
			if (StringUtils.hasText(attachmentMaxSize)) {
				swfUpload.setFileSizeLimit(attachmentMaxSize);
			}
		}
	}
}