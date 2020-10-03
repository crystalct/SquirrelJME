// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package cc.squirreljme.vm.springcoat;

import cc.squirreljme.jvm.mle.UIFormShelf;
import cc.squirreljme.jvm.mle.brackets.UIDisplayBracket;
import cc.squirreljme.jvm.mle.brackets.UIFormBracket;
import cc.squirreljme.jvm.mle.brackets.UIItemBracket;
import cc.squirreljme.jvm.mle.brackets.UIWidgetBracket;
import cc.squirreljme.jvm.mle.callbacks.UIFormCallback;
import cc.squirreljme.runtime.cldc.debug.Debugging;
import cc.squirreljme.vm.springcoat.brackets.UIDisplayObject;
import cc.squirreljme.vm.springcoat.brackets.UIFormObject;
import cc.squirreljme.vm.springcoat.brackets.UIItemObject;
import cc.squirreljme.vm.springcoat.brackets.UIWidgetObject;
import cc.squirreljme.vm.springcoat.exceptions.SpringMLECallError;

/**
 * SpringCoat support for {@link UIFormShelf}.
 *
 * @since 2020/06/30
 */
public enum MLEUIForm
	implements MLEFunction
{
	/** {@link UIFormShelf#callback(UIFormBracket, UIFormCallback)}. */
	CALLBACK("callback:(Lcc/squirreljme/jvm/mle/brackets/" +
		"UIFormBracket;Lcc/squirreljme/jvm/mle/callbacks/UIFormCallback;)V")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/09/13
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			UIFormObject form = MLEUIForm.__form(__args[0]);
			SpringObject callback = (SpringObject)__args[1];
			
			if (callback == null)
				throw new SpringMLECallError("Null callback.");
			
			// Since the callback is in SpringCoat, it has to be wrapped to
			// forward calls into, there also needs to be threads to do work
			// in
			UIFormShelf.callback(form.form,
				new UIFormCallbackAdapter(__thread.machine, callback));
			return null;
		}
	}, 
	
	/** {@link UIFormShelf#displays()}. */
	DISPLAYS("displays:()[Lcc/squirreljme/jvm/mle/brackets/" +
		"UIDisplayBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/07/01
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			UIDisplayBracket[] natives = UIFormShelf.displays();
			
			// Wrap the array
			int n = natives.length;
			SpringObject[] result = new SpringObject[n];
			for (int i = 0; i < n; i++)
				result[i] = new UIDisplayObject(natives[i]);
			
			// Use array as result
			return __thread.asVMObjectArray(__thread.resolveClass(
				"[Lcc/squirreljme/jvm/mle/brackets/UIDisplayBracket;"),
				result);
		}
	},
	
	/** {@link UIFormShelf#displayShow(UIDisplayBracket, UIFormBracket)}. */ 
	DISPLAY_SHOW("displayShow:(Lcc/squirreljme/jvm/mle/brackets/" +
		"UIDisplayBracket;Lcc/squirreljme/jvm/mle/brackets/UIFormBracket;)V")
	{
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			UIFormShelf.displayShow(MLEUIForm.__display(__args[0]).display,
				MLEUIForm.__form(__args[1]).form);
			return null;
		}
	},
	
	/** {@link UIFormShelf#equals(UIDisplayBracket, UIDisplayBracket)}. */
	EQUALS_DISPLAY("equals:(Lcc/squirreljme/jvm/mle/brackets/" +
		"UIDisplayBracket;Lcc/squirreljme/jvm/mle/brackets/" +
		"UIDisplayBracket;)Z")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/09/20
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return UIFormShelf.equals(MLEUIForm.__display(__args[0]).display,
				MLEUIForm.__display(__args[1]).display);
		}
	},
	
	/** {@link UIFormShelf#equals(UIFormBracket, UIFormBracket)}. */
	EQUALS_FORM("equals:(Lcc/squirreljme/jvm/mle/brackets/" +
		"UIFormBracket;Lcc/squirreljme/jvm/mle/brackets/" +
		"UIFormBracket;)Z")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/09/20
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return UIFormShelf.equals(MLEUIForm.__form(__args[0]).form,
				MLEUIForm.__form(__args[1]).form);
		}
	},
	
	/** {@link UIFormShelf#equals(UIItemBracket, UIItemBracket)}. */
	EQUALS_ITEM("equals:(Lcc/squirreljme/jvm/mle/brackets/" +
		"UIItemBracket;Lcc/squirreljme/jvm/mle/brackets/" +
		"UIItemBracket;)Z")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/09/20
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return UIFormShelf.equals(MLEUIForm.__item(__args[0]).item,
				MLEUIForm.__item(__args[1]).item);
		}
	},
	
	/** {@link UIFormShelf#equals(UIWidgetBracket, UIWidgetBracket)}. */
	EQUALS_WIDGET("equals:(Lcc/squirreljme/jvm/mle/brackets/" +
		"UIWidgetBracket;Lcc/squirreljme/jvm/mle/brackets/" +
		"UIWidgetBracket;)Z")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/09/20
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return UIFormShelf.equals(MLEUIForm.__widget(__args[0]).widget(),
				MLEUIForm.__widget(__args[1]).widget());
		}
	},
	
	/** {@link UIFormShelf#formDelete(UIFormBracket)}. */
	FORM_DELETE("formDelete:(Lcc/squirreljme/jvm/mle/brackets/" +
		"UIFormBracket;)V")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/07/01
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			UIFormShelf.formDelete(MLEUIForm.__form(__args[0]).form);
			return null;
		}
	},
	
	/** {@link UIFormShelf#formItemAtPosition(UIFormBracket, int)}. */
	FORM_ITEM_AT_POSITION("")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/10/03
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return new UIItemObject(UIFormShelf.formItemAtPosition(
				MLEUIForm.__form(__args[0]).form,
				(int)__args[1]));
		}
	}, 
	
	/** {@link UIFormShelf#formItemPosition(UIFormBracket, UIItemBracket)}. */
	FORM_ITEM_POSITION_GET("formItemPosition:(Lcc/squirreljme/jvm/mle/" +
		"brackets/UIFormBracket;Lcc/squirreljme/jvm/mle/brackets/" +
		"UIItemBracket;)I")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/07/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return UIFormShelf.formItemPosition(
				MLEUIForm.__form(__args[0]).form,
				MLEUIForm.__item(__args[1]).item);
		}
	},
	
	/**
	 * {@link
	 * UIFormShelf#formItemPosition(UIFormBracket, UIItemBracket, int)}.
	 */
	FORM_ITEM_POSITION_SET("formItemPosition:(Lcc/squirreljme/jvm/" +
		"mle/brackets/UIFormBracket;Lcc/squirreljme/jvm/mle/brackets/" +
		"UIItemBracket;I)V")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/07/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			UIFormShelf.formItemPosition(
				MLEUIForm.__form(__args[0]).form,
				MLEUIForm.__item(__args[1]).item,
				(int)__args[2]);
			return null;
		}
	},
	
	/** {@link UIFormShelf#formNew()}. */
	FORM_NEW("formNew:()Lcc/squirreljme/jvm/mle/brackets/UIFormBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/07/01
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return new UIFormObject(UIFormShelf.formNew());
		}
	},
	
	/** {@link UIFormShelf#itemDelete(UIItemBracket)}. */
	ITEM_DELETE("itemDelete:(Lcc/squirreljme/jvm/mle/brackets/" +
		"UIItemBracket;)V")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/07/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			UIFormShelf.itemDelete(MLEUIForm.__item(__args[0]).item);
			return null;
		}
	},
	
	/** {@link UIFormShelf#itemNew(int)}. */  
	ITEM_NEW("itemNew:(I)Lcc/squirreljme/jvm/mle/brackets/" +
		"UIItemBracket;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/07/18
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return new UIItemObject(UIFormShelf.itemNew((int)__args[0]));
		}
	},
	
	/** {@link UIFormShelf#metric(int)}. */
	METRIC("metric:(I)I")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/06/30
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			int metric = (int)__args[0];
			
			return UIFormShelf.metric(metric);
		}
	},
	
	/** {@link UIFormShelf#widgetPropertyInt(UIWidgetBracket, int)}. */
	WIDGET_PROPERTY_GET_INT("widgetPropertyInt:(Lcc/squirreljme/jvm/" +
		"mle/brackets/UIWidgetBracket;I)I")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/09/21
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return UIFormShelf.widgetPropertyInt(
				MLEUIForm.__widget(__args[0]).widget(),
				(int)__args[1]);
		}
	},
	
	/** {@link UIFormShelf#widgetPropertyStr(UIWidgetBracket, int)}. */
	WIDGET_PROPERTY_GET_SET("widgetPropertyStr:(Lcc/squirreljme/jvm/" +
		"mle/brackets/UIWidgetBracket;I)Ljava/lang/String;")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/09/21
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			return UIFormShelf.widgetPropertyStr(
				MLEUIForm.__widget(__args[0]).widget(),
				(int)__args[1]);
		}
	},
	
	/** {@link UIFormShelf#widgetProperty(UIWidgetBracket, int, int)}. */ 
	WIDGET_PROPERTY_SET_INT("widgetProperty:(Lcc/squirreljme/jvm/mle/" +
		"brackets/UIWidgetBracket;II)V")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/09/13
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			UIFormShelf.widgetProperty(
				MLEUIForm.__widget(__args[0]).widget(),
				(int)__args[1],
				(int)__args[2]);
			return null;
		}
	},
	
	/** {@link UIFormShelf#widgetProperty(UIWidgetBracket, int, int)}. */ 
	WIDGET_PROPERTY_SET_STR("widgetProperty:(Lcc/squirreljme/jvm/mle/" +
		"brackets/UIWidgetBracket;ILjava/lang/String;)V")
	{
		/**
		 * {@inheritDoc}
		 * @since 2020/09/13
		 */
		@Override
		public Object handle(SpringThreadWorker __thread, Object... __args)
		{
			UIFormShelf.widgetProperty(
				MLEUIForm.__widget(__args[0]).widget(),
				(int)__args[1],
				__thread.<String>asNativeObject(String.class, __args[2]));
			return null;
		}
	},
	
	/* End. */
	;
	
	/** The dispatch key. */
	protected final String key;
	
	/**
	 * Initializes the dispatcher info.
	 *
	 * @param __key The key.
	 * @throws NullPointerException On null arguments.
	 * @since 2020/06/30
	 */
	MLEUIForm(String __key)
		throws NullPointerException
	{
		if (__key == null)
			throw new NullPointerException("NARG");
		
		this.key = __key;
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2020/06/30
	 */
	@Override
	public String key()
	{
		return this.key;
	}
	
	/**
	 * Gets the object as a {@link UIDisplayObject}.
	 * 
	 * @param __o The object.
	 * @return As the desired {@link UIDisplayObject}.
	 * @throws SpringMLECallError If the object is not this type.
	 * @since 2020/07/01
	 */
	static UIDisplayObject __display(Object __o)
		throws SpringMLECallError
	{
		if (!(__o instanceof UIDisplayObject))
			throw new SpringMLECallError("Not a UIDisplayObject.");
		
		return (UIDisplayObject)__o;
	}
	
	/**
	 * Gets the object as a {@link UIFormObject}.
	 * 
	 * @param __o The object.
	 * @return As the desired {@link UIFormObject}.
	 * @throws SpringMLECallError If the object is not this type.
	 * @since 2020/07/01
	 */
	static UIFormObject __form(Object __o)
		throws SpringMLECallError
	{
		if (!(__o instanceof UIFormObject))
			throw new SpringMLECallError("Not a UIFormObject.");
		
		return (UIFormObject)__o;
	}
	
	/**
	 * Gets the object as a {@link UIItemObject}.
	 * 
	 * @param __o The object.
	 * @return As the desired {@link UIItemObject}.
	 * @throws SpringMLECallError If the object is not this type.
	 * @since 2020/07/01
	 */
	static UIItemObject __item(Object __o)
		throws SpringMLECallError
	{
		if (!(__o instanceof UIItemObject))
			throw new SpringMLECallError("Not a UIItemObject.");
		
		return (UIItemObject)__o;
	}
	
	/**
	 * Gets the widget from this object.
	 * 
	 * @param __o The object to get from.
	 * @throws SpringMLECallError If not one.
	 * @return The widget.
	 * @since 2020/09/20
	 */
	static UIWidgetObject __widget(Object __o)
		throws SpringMLECallError
	{
		if (!(__o instanceof UIWidgetObject))
			throw new SpringMLECallError("Not a UIWidgetObject.");
		
		return (UIWidgetObject)__o;
	}
}
