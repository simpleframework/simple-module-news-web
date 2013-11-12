package net.simpleframework.module.news.web;

import java.io.File;
import java.util.Map;

import net.simpleframework.ado.bean.IIdBeanAware;
import net.simpleframework.common.Convert;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.EContentStatus;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.module.log.EntityUpdateLog;
import net.simpleframework.module.log.LogRef;
import net.simpleframework.module.log.web.page.DownloadLogPage;
import net.simpleframework.module.log.web.page.EntityUpdateLogPage;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.web.page.t1.NewsMgrPage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.Option;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.window.WindowBean;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885)
 *         http://code.google.com/p/simpleframework/
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

		@Override
		protected TablePagerColumn newColumn(final String name) {
			if (COL_FROMVAL.equals(name) || COL_TOVAL.equals(name)) {
				return new TablePagerColumn(name) {
					@Override
					protected Option[] getFilterOptions() {
						return Option.from(NewsMgrPage.STATUS_ARR);
					}
				};
			}
			return super.newColumn(name);
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp,
				final EntityUpdateLog field) {
			final KVMap kv = (KVMap) super.getRowData(cp, field);
			final EContentStatus from = Convert.toEnum(EContentStatus.class, field.getFromVal());
			final StringBuilder sb = new StringBuilder();
			sb.append(new SpanElement().setClassName("news_status_" + from.name())).append(from);
			kv.put(COL_FROMVAL, sb.toString());
			final EContentStatus to = Convert.toEnum(EContentStatus.class, field.getToVal());
			sb.setLength(0);
			sb.append(new SpanElement().setClassName("news_status_" + to.name())).append(to);
			kv.put(COL_TOVAL, sb.toString());
			return kv;
		}
	}

	public static class NewsDownloadLogPage extends DownloadLogPage implements INewsContextAware {

		@Override
		protected IIdBeanAware getBean(final PageParameter pp) {
			return context.getAttachmentService().getBean(pp.getParameter(getBeanIdParameter()));
		}
	}
}