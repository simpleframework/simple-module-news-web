package net.simpleframework.module.news.web;

import java.io.File;

import net.simpleframework.ctx.IModuleRef;
import net.simpleframework.module.common.web.content.IContentRefAware;
import net.simpleframework.module.news.INewsContext;
import net.simpleframework.module.news.INewsContextAware;
import net.simpleframework.mvc.IMVCContextVar;
import net.simpleframework.mvc.common.IDownloadHandler;

/**
 * Licensed under the Apache License, Version 2.0
 * 
 * @author 陈侃(cknet@126.com, 13910090885) https://github.com/simpleframework
 *         http://www.simpleframework.net
 */
public interface INewsWebContext extends INewsContext, IContentRefAware, IMVCContextVar {

	/**
	 * 获取小页面的创建类
	 * 
	 * @return
	 */
	NewsPageletCreator getPageletCreator();

	NewsUrlsFactory getUrlsFactory();

	/**
	 * 获取标签模块的引用
	 * 
	 * @return
	 */
	IModuleRef getTagRef();

	IModuleRef getVoteRef();

	public static class AttachmentDownloadHandler implements IDownloadHandler, INewsContextAware {

		@Override
		public void onDownloaded(final Object beanId, final String topic, final File oFile) {
			final IModuleRef ref = ((INewsWebContext) context).getLogRef();
			if (ref != null) {
				// 记录下载日志
				((NewsLogRef) ref).logDownload(beanId, topic, oFile);
			}
		}
	}
}
