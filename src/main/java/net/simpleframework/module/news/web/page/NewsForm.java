package net.simpleframework.module.news.web.page;

import static net.simpleframework.common.I18n.$m;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.simpleframework.common.Convert;
import net.simpleframework.common.StringUtils;
import net.simpleframework.common.object.ObjectUtils;
import net.simpleframework.common.web.HttpUtils;
import net.simpleframework.common.web.html.HtmlUtils;
import net.simpleframework.common.web.html.HtmlUtils.IElementVisitor;
import net.simpleframework.ctx.common.bean.AttachmentFile;
import net.simpleframework.ctx.trans.Transaction;
import net.simpleframework.lib.org.jsoup.nodes.Document;
import net.simpleframework.module.common.content.Attachment;
import net.simpleframework.module.common.content.ContentException;
import net.simpleframework.module.common.content.IAttachmentService;
import net.simpleframework.module.log.web.hdl.AbstractAttachmentLogHandler;
import net.simpleframework.module.news.INewsContext;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.module.news.INewsService;
import net.simpleframework.module.news.News;
import net.simpleframework.module.news.NewsCategory;
import net.simpleframework.module.news.web.INewsWebContext;
import net.simpleframework.module.news.web.page.t1.NewsMgrPage;
import net.simpleframework.mvc.AbstractMVCPage;
import net.simpleframework.mvc.JavascriptForward;
import net.simpleframework.mvc.PageParameter;
import net.simpleframework.mvc.common.element.AbstractElement;
import net.simpleframework.mvc.common.element.BlockElement;
import net.simpleframework.mvc.common.element.ButtonElement;
import net.simpleframework.mvc.common.element.Checkbox;
import net.simpleframework.mvc.common.element.ElementList;
import net.simpleframework.mvc.common.element.ImageElement;
import net.simpleframework.mvc.common.element.InputElement;
import net.simpleframework.mvc.common.element.LinkButton;
import net.simpleframework.mvc.common.element.LinkElement;
import net.simpleframework.mvc.common.element.RowField;
import net.simpleframework.mvc.common.element.SpanElement;
import net.simpleframework.mvc.common.element.TableRow;
import net.simpleframework.mvc.common.element.TableRows;
import net.simpleframework.mvc.common.element.TextButton;
import net.simpleframework.mvc.component.AbstractComponentBean;
import net.simpleframework.mvc.component.ComponentParameter;
import net.simpleframework.mvc.component.base.validation.EValidatorMethod;
import net.simpleframework.mvc.component.base.validation.Validator;
import net.simpleframework.mvc.component.ext.attachments.AttachmentBean;
import net.simpleframework.mvc.component.ext.attachments.AttachmentUtils;
import net.simpleframework.mvc.component.ext.attachments.IAttachmentSaveCallback;
import net.simpleframework.mvc.component.ext.category.ICategoryHandler;
import net.simpleframework.mvc.component.ui.dictionary.DictionaryBean;
import net.simpleframework.mvc.component.ui.dictionary.DictionaryTreeHandler;
import net.simpleframework.mvc.component.ui.htmleditor.HtmlEditorBean;
import net.simpleframework.mvc.component.ui.htmleditor.Toolbar;
import net.simpleframework.mvc.component.ui.tree.TreeBean;
import net.simpleframework.mvc.component.ui.tree.TreeNode;
import net.simpleframework.mvc.component.ui.tree.TreeNodes;
import net.simpleframework.mvc.component.ui.window.WindowBean;
import net.simpleframework.mvc.template.lets.FormTableRowTemplatePage;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public class NewsForm extends FormTableRowTemplatePage implements INewsContextAware {

	@Override
	protected void addComponents(final PageParameter pp) {
		super.addComponents(pp);

		addFormValidationBean(pp).addValidators(
				new Validator(EValidatorMethod.required, "#ne_topic, #ne_categoryText, #ne_content"));

		// Html编辑器
		addHtmlEditorBean(pp).setTextarea("ne_content");

		// 类目字典
		addComponentBean(pp, "NewsForm_dict_tree", TreeBean.class).setHandleClass(
				CategorySelectedTree.class);
		addComponentBean(pp, "NewsForm_dict", DictionaryBean.class).setBindingId("ne_categoryId")
				.setBindingText("ne_categoryText").addTreeRef(pp, "NewsForm_dict_tree")
				.setTitle($m("NewsForm.1"));

		// 上传
		addComponentBean(pp, "NewsForm_upload_page", AttachmentBean.class).setInsertTextarea(
				"ne_content").setHandleClass(NewsAttachmentAction.class);
		addComponentBean(pp, "NewsForm_upload", WindowBean.class)
				.setContentRef("NewsForm_upload_page").setTitle($m("NewsForm.10")).setPopup(true)
				.setHeight(480).setWidth(400);
	}

	protected HtmlEditorBean addHtmlEditorBean(final PageParameter pp) {
		return (HtmlEditorBean) addHtmlEditorBean(pp, "NewsForm_editor").setToolbar(Toolbar.STANDARD)
				.setHeight("340");
	}

	@Override
	@Transaction(context = INewsContext.class)
	public JavascriptForward onSave(final ComponentParameter cp) {
		final NewsCategory category = context.getNewsCategoryService().getBean(
				cp.getParameter("ne_categoryId"));
		if (category == null) {
			throw ContentException.of($m("NewsForm.9"));
		}

		final String ne_cname = cp.getParameter("ne_cname");
		final INewsService service = context.getNewsService();
		News news = service.getBean(cp.getParameter("ne_id"));
		final boolean insert = (news == null);
		if (insert) {
			news = service.createBean();
			news.setCreateDate(new Date());
			news.setUserId(cp.getLoginId());
		} else {
			if (!ObjectUtils.objectEquals(news.getCname(), ne_cname)
					&& service.getBeanByName(ne_cname) != null) {
				throw ContentException.of("已存在相同的名称: " + ne_cname);
			}
		}

		news.setCategoryId(category.getId());
		news.setDomain(context.getDomain());
		news.setCname(ne_cname);
		news.setTopic(cp.getParameter("ne_topic"));
		news.setKeyWords(cp.getParameter("ne_keyWords"));
		news.setSource(cp.getParameter("ne_source"));
		news.setAuthor(cp.getParameter("ne_author"));

		final Document doc = HtmlUtils.createHtmlDocument(cp.getParameter("ne_content"));
		news.setContent(doNewsContent(cp, news, doc));

		news.setDescription(cp.getParameter("ne_description"));
		news.setAllowComments(cp.getBoolParameter(OPT_ALLOWCOMMENTS));
		news.setIndexed(cp.getBoolParameter(OPT_INDEXED));
		news.setImageMark(cp.getBoolParameter(OPT_IMAGEMARK));

		final News news2 = news;
		final ComponentParameter nCP = ComponentParameter.get(cp,
				cp.getComponentBeanByName("NewsForm_upload_page"));
		AttachmentUtils.doSave(nCP, new IAttachmentSaveCallback() {
			@Override
			public void save(final Map<String, AttachmentFile> addQueue, final Set<String> deleteQueue) {
				final IAttachmentService<Attachment> aService = context.getAttachmentService();
				if (insert) {
					service.insert(news2);
				} else {
					service.update(news2);
					if (deleteQueue != null) {
						aService.delete(deleteQueue.toArray(new Object[] { deleteQueue.size() }));
					}
				}
				aService.insert(news2.getId(), cp.getLoginId(), addQueue);
			}
		});
		return doSaveForward(cp, news);
	}

	protected JavascriptForward doSaveForward(final ComponentParameter cp, final News news) {
		final JavascriptForward js = new JavascriptForward();
		js.append("$Actions.loc('")
				.append(((INewsWebContext) context).getUrlsFactory().getNewsFormUrl(news))
				.append("&op=save");
		final String url = cp.getParameter("url");
		if (StringUtils.hasText(url)) {
			js.append("&url=").append(HttpUtils.encodeUrl(url));
		}
		js.append("');");
		return js;
	}

	protected String doNewsContent(final PageParameter pp, final News news, final Document doc) {
		final ArrayList<IElementVisitor> al = new ArrayList<IElementVisitor>();
		al.add(HtmlUtils.REMOVE_TAG_VISITOR("script"));
		al.add(HtmlUtils.STRIP_CONTEXTPATH_VISITOR(pp.request));
		if (pp.getBoolParameter(OPT_REMOVE_CLASS)) {
			setVisitor_removeClass(news, al);
		}
		if (pp.getBoolParameter(OPT_REMOVE_STYLE)) {
			setVisitor_removeStyle(news, al);
		}
		if (pp.getBoolParameter(OPT_TARGET_BLANK)) {
			setVisitor_targetBlank(news, al);
		}
		return HtmlUtils.doDocument(doc, al.toArray(new IElementVisitor[al.size()])).html();
	}

	protected void setVisitor_removeClass(final News news, final List<IElementVisitor> al) {
		al.add(HtmlUtils.REMOVE_ATTRI_VISITOR("class"));
	}

	protected void setVisitor_removeStyle(final News news, final List<IElementVisitor> al) {
		al.add(HtmlUtils.REMOVE_ATTRI_VISITOR("style"));
	}

	protected void setVisitor_targetBlank(final News news, final List<IElementVisitor> al) {
		al.add(HtmlUtils.TARGET_BLANK_VISITOR);
	}

	@Override
	protected TableRows getTableRows(final PageParameter pp) {
		final InputElement ne_id = InputElement.hidden("ne_id");
		final InputElement ne_topic = new InputElement("ne_topic");
		final InputElement ne_categoryId = InputElement.hidden("ne_categoryId");
		final TextButton ne_categoryText = new TextButton("ne_categoryText")
				.setOnclick("$Actions['NewsForm_dict']()");

		final InputElement ne_keyWords = new InputElement("ne_keyWords");
		final InputElement ne_source = new InputElement("ne_source");
		final InputElement ne_author = new InputElement("ne_author");

		final InputElement ne_description = InputElement.textarea("ne_description").setRows(3);
		final InputElement ne_content = InputElement.textarea("ne_content")
				.setStyle("display: none;");

		final InputElement ne_cname = new InputElement("ne_cname");

		NewsCategory category = null;
		final News news = NewsViewTPage.getNews(pp);
		if (news != null) {
			ne_id.setText(news.getId());
			ne_cname.setText(news.getCname());
			ne_topic.setText(news.getTopic());
			ne_keyWords.setText(news.getKeyWords());
			ne_source.setText(news.getSource());
			ne_author.setText(news.getAuthor());
			ne_content.setText(HtmlUtils.wrapContextPath(pp.request, news.getContent()));
			ne_description.setText(news.getDescription());
			category = context.getNewsCategoryService().getBean(news.getCategoryId());
		} else {
			final String _ne_cname = pp.getParameter("ne_cname");
			if (StringUtils.hasText(_ne_cname)) {
				ne_cname.setText(_ne_cname);
			}
		}
		if (category == null) {
			category = context.getNewsCategoryService().getBean(pp.getParameter("categoryId"));
		}
		if (category != null) {
			ne_categoryId.setText(category.getId());
			ne_categoryText.setText(category.getText());
		}

		final TableRow r1 = new TableRow(
				new RowField($m("NewsForm.0"), ne_id, ne_topic).setStarMark(true), new RowField(
						$m("NewsForm.1"), ne_categoryId, ne_categoryText)
						.setElementsStyle("width:150px;").setStarMark(true));
		if (pp.getLogin().isManager()) {
			// 唯一名称，保留给系统管理员
			r1.append(new RowField($m("NewsForm.13"), ne_cname).setElementsStyle("width:150px;"));
		}
		final TableRow r2 = new TableRow(new RowField($m("NewsForm.2"), ne_keyWords), new RowField(
				$m("NewsForm.3"), ne_source).setElementsStyle("width:150px;"), new RowField(
				$m("NewsForm.4"), ne_author).setElementsStyle("width:150px;"));
		final TableRow r3 = new TableRow(new RowField($m("NewsForm.5"), ne_content).setStarMark(true));
		final TableRow r4 = new TableRow(new RowField($m("NewsForm.6"), ne_description));
		final TableRows rows = TableRows.of(r1, r2, r3, r4);
		return rows;
	}

	public static final String OPT_ALLOWCOMMENTS = "opt_allowComments";

	public static final String OPT_INDEXED = "opt_indexed";

	public static final String OPT_IMAGEMARK = "opt_imageMark"; // 图片新闻

	//
	public static final String OPT_VIEWER = "opt_viewer";

	public static final String OPT_TARGET_BLANK = "opt_targetBlank";

	public static final String OPT_REMOVE_CLASS = "opt_removeClass";

	public static final String OPT_REMOVE_STYLE = "opt_removeStyle";

	@Override
	public ElementList getLeftElements(final PageParameter pp) {
		final Checkbox opt_allowComments = new Checkbox(OPT_ALLOWCOMMENTS, $m("NewsForm.8"));
		final Checkbox opt_indexed = new Checkbox(OPT_INDEXED, $m("NewsForm.14"));
		final Checkbox opt_imageMark = new Checkbox(OPT_IMAGEMARK, $m("NewsForm.11"));

		final Checkbox opt_viewer = new Checkbox(OPT_VIEWER, $m("NewsForm.12")).setChecked(true);

		final Checkbox opt_targetBlank = new Checkbox(OPT_TARGET_BLANK, $m("NewsForm.16"))
				.setChecked(true);
		final Checkbox opt_removeClass = new Checkbox(OPT_REMOVE_CLASS, $m("NewsForm.17"))
				.setChecked(true);
		final Checkbox opt_removeStyle = new Checkbox(OPT_REMOVE_STYLE, $m("NewsForm.18"));

		final News news = NewsViewTPage.getNews(pp);
		String attachClick = "$Actions['NewsForm_upload']('" + OPT_VIEWER + "=' + $F('" + OPT_VIEWER
				+ "')";
		if (news != null) {
			opt_allowComments.setChecked(news.isAllowComments());
			opt_indexed.setChecked(news.isIndexed());
			opt_imageMark.setChecked(news.isImageMark());
			attachClick += " + '&newsId=" + news.getId() + "'";
		} else {
			final News _news = new News();
			opt_allowComments.setChecked(_news.isAllowComments());
			opt_indexed.setChecked(_news.isIndexed());
			opt_imageMark.setChecked(_news.isImageMark());
		}
		attachClick += ");";

		final ElementList el = ElementList.of();
		el.append(opt_allowComments)
				.append(SpanElement.SPACE)
				.append(opt_imageMark)
				.append(SpanElement.SPACE)
				.append(opt_indexed)
				.append(SpanElement.SPACE)
				.append(new LinkButton($m("NewsForm.15")).setOnclick("$('idNewsForm_opts').toggle();"))
				.append(SpanElement.SPACE)
				.append(new LinkButton($m("NewsForm.7")).setOnclick(attachClick))
				.append(
						new BlockElement("idNewsForm_opts").setStyle("display: none;").addElements(
								opt_viewer, SpanElement.SPACE, opt_targetBlank, SpanElement.SPACE,
								opt_removeClass, SpanElement.SPACE, opt_removeStyle));
		return el;
	}

	@Override
	public ElementList getRightElements(final PageParameter pp) {
		final News news = NewsViewTPage.getNews(pp);
		final ElementList el = ElementList.of();
		if (news != null) {
			el.append(
					new ButtonElement($m("Button.Preview")).setOnclick("$Actions.loc('"
							+ ((INewsWebContext) context).getUrlsFactory().getNewsUrl(news, true)
							+ "', true);"), SpanElement.SPACE);
		}
		el.append(SAVE_BTN());
		return el;
	}

	@Override
	public boolean isButtonsOnTop(final PageParameter pp) {
		return true;
	}

	@Override
	public int getLabelWidth(final PageParameter pp) {
		return 70;
	}

	protected TreeNodes getCategoryDictTreenodes(final ComponentParameter cp, final TreeNode parent) {
		final AbstractComponentBean categoryBean = AbstractMVCPage.get(NewsMgrPage.class)
				.getCategoryBean();
		if (categoryBean == null) {
			return null;
		}
		final ComponentParameter nCP = ComponentParameter.get(cp, categoryBean);
		final ICategoryHandler cHandle = (ICategoryHandler) nCP.getComponentHandler();
		return cHandle.getCategoryDictTreenodes(nCP, (TreeBean) cp.componentBean, parent);
	}

	public static class CategorySelectedTree extends DictionaryTreeHandler {

		@Override
		public TreeNodes getTreenodes(final ComponentParameter cp, final TreeNode parent) {
			return ((NewsForm) get(cp)).getCategoryDictTreenodes(cp, parent);
		}
	}

	public static class NewsAttachmentAction extends AbstractAttachmentLogHandler<Attachment, News> {

		@Override
		protected IAttachmentService<Attachment> getAttachmentService() {
			return context.getAttachmentService();
		}

		@Override
		protected INewsService getOwnerService() {
			return context.getNewsService();
		}

		@Override
		protected String getOwnerIdParameterKey() {
			return "newsId";
		}

		@Override
		public AbstractElement<?> getDownloadLink(final ComponentParameter cp,
				final AttachmentFile attachmentFile, final String id) {
			if (Convert.toBool(cp.getParameter(NewsForm.OPT_VIEWER))) {
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
