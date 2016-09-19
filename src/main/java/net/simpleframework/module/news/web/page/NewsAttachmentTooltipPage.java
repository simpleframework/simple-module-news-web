package net.simpleframework.module.news.web.page;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;

import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.module.common.web.content.page.AbstractAttachmentTooltipPage;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.bean.NewsAttachment;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.NewsLogRef.NewsDownloadLogPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsAttachmentTooltipPage extends AbstractAttachmentTooltipPage
		implements INewsContextAware {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		if (((INewsWebContext) newsContext).getLogRef() != null) {
			final AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "AttachmentTooltipPage_logPage",
					NewsDownloadLogPage.class);
			addWindowBean(pp, "AttachmentTooltipPage_logWin", ajaxRequest).setHeight(480).setWidth(800)
					.setTitle($m("NewsFormAttachPage.5"));
		}
	}

	@Override
	protected AttachmentFile getAttachment(final PageParameter pp) {
		final IAttachmentService<NewsAttachment> service = newsContext.getAttachmentService();
		try {
			return service.createAttachmentFile(service.getBean(pp.getParameter("id")));
		} catch (final IOException e) {
			return null;
		}
	}

	@Override
	protected Object getTopic(final PageParameter pp, final AttachmentFile attachment) {
		return new LinkElement(attachment.getTopic())
				.setOnclick("$Actions['NewsViewTPage_download']('id=" + attachment.getId() + "');");
	}

	@Override
	protected Object getDownloads(final PageParameter pp, final AttachmentFile attachment) {
		final int downloads = attachment.getDownloads();
		if (downloads <= 0) {
			return 0;
		}
		if (((INewsWebContext) newsContext).getLogRef() != null) {
			return LinkElement.style2(downloads).setOnclick(
					"$Actions['AttachmentTooltipPage_logWin']('beanId=" + attachment.getId() + "');");
		} else {
			return downloads;
		}
	}
}