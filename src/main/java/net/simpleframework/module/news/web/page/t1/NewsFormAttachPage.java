package net.simpleframework.module.news.web.page.t1;

import static net.simpleframework.common.I18n.$m;

import java.io.IOException;
import java.util.Map;

import net.simpleframework.ado.query.IDataQuery;
import net.simpleframework.common.FileUtils;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.coll.ArrayUtils;
import net.simpleframework.common.coll.KVMap;
import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.module.news.ENewsAttach;
import net.simpleframework.module.news.INewsContext;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.NewsAttachment;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.NewsLogRef.NewsDownloadLogPage;
import net.simpleframework.module.news.web.page.NewsUtils;
import net.simpleframework.mvc.IForward;
import net.simpleframework.mvc.IMultipartFile;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageMapping;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.DownloadUtils;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.ETextAlign;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.JS;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.Option;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.ajaxrequest.AjaxRequestBean;
import net.simpleframework.mvc.component.ui.pager.EPagerBarLayout;
import net.simpleframework.mvc.component.ui.pager.TablePagerBean;
import net.simpleframework.mvc.component.ui.pager.TablePagerColumn;
import net.simpleframework.mvc.component.ui.pager.db.AbstractDbTablePagerHandler;
import net.simpleframework.mvc.component.ui.swfupload.AbstractSwfUploadHandler;
import net.simpleframework.mvc.component.ui.swfupload.SwfUploadBean;
import net.simpleframework.mvc.template.AbstractTemplatePage;
import net.simpleframework.mvc.template.lets.FormTableRowTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
@PageMapping(url = "/news/form/attach")
public class NewsFormAttachPage extends NewsFormBasePage {

	@Override
	protected void onForward(final PageParameter pp) throws Exception {
		super.onForward(pp);

		// 添加表格
		addTablePagerBean(pp);

		if (((INewsWebContext) newsContext).getLogRef() != null) {
			// 下载日志
			final AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "NewsTabAttachPage_logPage",
					NewsDownloadLogPage.class);
			addWindowBean(pp, "NewsTabAttachPage_logWin", ajaxRequest).setHeight(480).setWidth(800)
					.setTitle($m("NewsFormAttachPage.5"));
		}

		// 上传
		AjaxRequestBean ajaxRequest = addAjaxRequest(pp, "NewsFormAttachPage_uploadPage",
				AttachmentUploadPage.class);
		addWindowBean(pp, "NewsFormAttachPage_upload", ajaxRequest).setTitle($m("NewsFormTPage.10"))
				.setPopup(true).setHeight(480).setWidth(400);

		// 删除
		addDeleteAjaxRequest(pp, "NewsFormAttachPage_delete");

		// 编辑
		ajaxRequest = addAjaxRequest(pp, "NewsFormAttachPage_editPage", AttachmentEditPage.class);
		addWindowBean(pp, "NewsFormAttachPage_edit", ajaxRequest).setHeight(280).setWidth(420)
				.setTitle($m("AttachmentEditPage.0"));
	}

	@Override
	protected boolean isPage404(final PageParameter pp) {
		return NewsUtils.getNews(pp) == null;
	}

	protected TablePagerBean addTablePagerBean(final PageParameter pp) {
		final TablePagerBean tablePager = (TablePagerBean) super
				.addTablePagerBean(pp, "NewsTabAttachPage_tbl", NewsAttachmentTbl.class)
				.setPagerBarLayout(EPagerBarLayout.bottom).setContainerId("idNewsTabAttachPage_tbl");
		tablePager
				.addColumn(new TablePagerColumn("topic", $m("NewsFormAttachPage.0")).setSort(false))
				.addColumn(
						new TablePagerColumn("attachsize", $m("NewsFormAttachPage.1"), 80)
								.setFilter(false))
				.addColumn(
						new TablePagerColumn("downloads", $m("NewsFormAttachPage.2"), 80).setTextAlign(
								ETextAlign.center).setFilter(false))
				.addColumn(
						new TablePagerColumn("userId", $m("NewsFormAttachPage.3"), 100)
								.setFilterSort(false))
				.addColumn(
						TablePagerColumn.DATE("createDate", $m("NewsFormAttachPage.4")).setFilter(false))
				.addColumn(TablePagerColumn.OPE(120));
		return tablePager;
	}

	@Transaction(context = INewsContext.class)
	public IForward doDelete(final ComponentParameter cp) {
		final Object[] ids = StringUtils.split(cp.getParameter("id"));
		newsContext.getAttachmentService().delete(ids);
		return new JavascriptForward("$Actions['NewsTabAttachPage_tbl']();");
	}

	@Override
	protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
			final String currentVariable) throws IOException {
		final StringBuilder sb = new StringBuilder();
		sb.append("<div class='NewsFormAttachPage'>");
		sb.append(" <div class='tbar'>");
		final News news = NewsUtils.getNews(pp);
		sb.append(LinkButton.corner($m("NewsFormAttachPage.6")).setOnclick(
				"$Actions['NewsFormAttachPage_upload']('newsId=" + news.getId() + "');"));
		sb.append(" </div>");
		sb.append(" <div id='idNewsTabAttachPage_tbl'></div>");
		sb.append("</div>");
		return sb.toString();
	}

	public static class NewsAttachmentTbl extends AbstractDbTablePagerHandler {
		@Override
		public IDataQuery<?> createDataObjectQuery(final ComponentParameter cp) {
			final News news = NewsUtils.getNews(cp);
			cp.addFormParameter("newsId", news.getId());
			return newsContext.getAttachmentService().queryByContent(news);
		}

		@Override
		protected Map<String, Object> getRowData(final ComponentParameter cp, final Object dataObject) {
			final NewsAttachment attachment = (NewsAttachment) dataObject;
			final KVMap kv = new KVMap();
			final Object id = attachment.getId();
			try {
				final AttachmentFile af = newsContext.getAttachmentService().createAttachmentFile(
						attachment);
				final String dloc = JS.loc(DownloadUtils.getDownloadHref(af, null), true);
				kv.put(
						"topic",
						new LinkElement(attachment.getTopic()).setOnclick(dloc).setTitle(
								attachment.getDescription()));
			} catch (final IOException e) {
				kv.put("topic", attachment.getTopic());
			}
			kv.put("attachsize", FileUtils.toFileSize(attachment.getAttachsize()));
			if (((INewsWebContext) newsContext).getLogRef() != null) {
				kv.put("downloads", new ButtonElement(attachment.getDownloads())
						.setOnclick("$Actions['NewsTabAttachPage_logWin']('beanId=" + id + "');"));
			} else {
				kv.put("downloads", attachment.getDownloads());
			}
			kv.put("userId", cp.getUser(attachment.getUserId()));
			kv.put("createDate", attachment.getCreateDate());
			final StringBuilder ope = new StringBuilder();
			ope.append(ButtonElement.editBtn().setOnclick(
					"$Actions['NewsFormAttachPage_edit']('beanId=" + id + "');"));
			ope.append(SpanElement.SPACE);
			ope.append(ButtonElement.deleteBtn().setOnclick(
					"$Actions['NewsFormAttachPage_delete']('id=" + id + "');"));
			kv.add(TablePagerColumn.OPE, ope);
			return kv;
		}
	}

	public static class AttachmentEditPage extends FormTableRowTemplatePage {

		@Transaction(context = INewsContext.class)
		@Override
		public JavascriptForward onSave(final ComponentParameter cp) throws Exception {
			final IAttachmentService<NewsAttachment> aService = newsContext.getAttachmentService();
			final NewsAttachment attachment = getCacheBean(cp, aService, "beanId");
			if (attachment != null) {
				attachment.setTopic(cp.getParameter("ae_topic"));
				attachment.setDescription(cp.getParameter("ae_description"));
				final ENewsAttach attachtype = cp.getEnumParameter(ENewsAttach.class, "ae_attachtype");
				if (attachtype != null) {
					attachment.setAttachtype(attachtype.ordinal());
				}
				aService.update(new String[] { "topic", "attachtype", "description" }, attachment);
			}
			final JavascriptForward js = super.onSave(cp);
			js.append("$Actions['NewsTabAttachPage_tbl']();");
			return js;
		}

		@Override
		protected TableRows getTableRows(final PageParameter pp) {
			final InputElement beanId = InputElement.hidden("beanId");
			final InputElement ae_topic = new InputElement("ae_topic");

			final Option[] opts = Option.from(ENewsAttach.values());
			final InputElement ae_description = InputElement.textarea("ae_description").setRows(4);

			final Attachment attachment = getCacheBean(pp, newsContext.getAttachmentService(),
					"beanId");
			if (attachment != null) {
				beanId.setText(attachment.getId());
				ae_topic.setText(attachment.getTopic());
				opts[attachment.getAttachtype()].setSelected(true);
				ae_description.setText(attachment.getDescription());
			}
			return TableRows.of(new TableRow(
					new RowField($m("AttachmentEditPage.1"), beanId, ae_topic)), new TableRow(
					new RowField($m("AttachmentEditPage.2"), InputElement.select("ae_attachtype")
							.addElements(opts))), new TableRow(new RowField($m("Description"),
					ae_description)));
		}
	}

	public static class AttachmentUploadPage extends AbstractTemplatePage {
		@Override
		protected void onForward(final PageParameter pp) throws Exception {
			super.onForward(pp);

			// 上传
			final SwfUploadBean swfUpload = (SwfUploadBean) addComponentBean(pp,
					"AttachmentUploadPage_swf", SwfUploadBean.class).setMultiFileSelected(true)
					.setJsCompleteCallback("$Actions['NewsTabAttachPage_tbl']();")
					.setContainerId("idAttachmentUploadPage_swf")
					.setHandlerClass(_SwfUploadHandler.class);
			final String attachmentMaxSize = ((INewsWebContext) newsContext).getAttachmentMaxSize();
			if (StringUtils.hasText(attachmentMaxSize)) {
				swfUpload.setFileSizeLimit(attachmentMaxSize);
			}
		}

		@Override
		protected String toHtml(final PageParameter pp, final Map<String, Object> variables,
				final String currentVariable) throws IOException {
			final StringBuilder sb = new StringBuilder();
			sb.append("<div id='idAttachmentUploadPage_swf'></div>");
			return sb.toString();
		}
	}

	public static class _SwfUploadHandler extends AbstractSwfUploadHandler {
		@Override
		public Map<String, Object> getFormParameters(final ComponentParameter cp) {
			final News news = NewsUtils.getNews(cp);
			return new KVMap().add("newsId", news.getId());
		}

		@Override
		public void upload(final ComponentParameter cp, final IMultipartFile multipartFile,
				final Map<String, Object> variables) throws Exception {
			final News news = NewsUtils.getNews(cp);
			final IAttachmentService<NewsAttachment> aService = newsContext.getAttachmentService();
			aService.insert(news.getId(), cp.getLoginId(),
					ArrayUtils.asList(new AttachmentFile(multipartFile.getFile())));
		}
	}
}
