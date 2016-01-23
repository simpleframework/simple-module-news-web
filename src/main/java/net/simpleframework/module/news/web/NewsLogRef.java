package net.simpleframework.module.news.web;

import java.io.File;
import java.io.IOException;

import net.simpleframework.common.Convert;
import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.ctx.service.ado.db.IDbBeanService;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.module.log.LogRef;
import net.simpleframework.module.log.web.hdl.AbstractAttachmentLogHandler;
import net.simpleframework.module.log.web.page.DownloadLogPage;
import net.simpleframework.module.log.web.page.EntityUpdateLogPage;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.INewsService;
import net.simpleframework.module.news.News;
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
	public void logDownload(final Object beanId, final String topic, final File oFile) {
		super.logDownload(beanId, topic, oFile);

		// 更新计数
		final IAttachmentService<Attachment> service = newsContext.getAttachmentService();
		final Attachment attachment = service.getBean(beanId);
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

	public static class NewsAttachmentAction extends AbstractAttachmentLogHandler<Attachment, News> {

		@Override
		protected IAttachmentService<Attachment> getAttachmentService() {
			return newsContext.getAttachmentService();
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
		public void setSwfUploadBean(final ComponentParameter cp, final SwfUploadBean swfUpload) {
			super.setSwfUploadBean(cp, swfUpload);
			swfUpload.setFileSizeLimit("50MB");
		}

		@Override
		public AbstractElement<?> getDownloadLink(final ComponentParameter cp,
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
	}
}