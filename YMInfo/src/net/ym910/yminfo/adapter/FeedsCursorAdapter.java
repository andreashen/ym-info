/**
 * YMInfo
 *
 * Copyright (c) 2012-2013 WeiQiang ym910.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.ym910.yminfo.adapter;

import net.ym910.yminfo.R;
import net.ym910.yminfo.provider.FeedData.FeedColumns;
import net.ym910.yminfo.utils.MD5Util;
import net.ym910.yminfo.utils.UiUtils;
import net.ym910.yminfo.view.DragNDropExpandableListView;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class FeedsCursorAdapter extends CursorLoaderExpandableListAdapter {

    private final Activity mActivity;
    private int isGroupPosition = -1;
    private int namePosition = -1;
    private int idPosition = -1;
    private int linkPosition = -1;
    private int iconPosition = -1;

    private DragNDropExpandableListView mListView;

    public FeedsCursorAdapter(Activity activity, Uri groupUri) {
        super(activity, groupUri, R.layout.feed_list_item, R.layout.feed_list_item);

        mActivity = activity;
    }

    public void setExpandableListView(DragNDropExpandableListView listView) {
        mListView = listView;
    }

    @Override
    protected void onCursorLoaded(Context context, Cursor cursor) {
        getCursorPositions(cursor);
    }

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor) {
        view.findViewById(R.id.indicator).setVisibility(View.INVISIBLE);

        TextView textView = ((TextView) view.findViewById(android.R.id.text1));
        byte[] iconBytes = cursor.getBlob(iconPosition);

        if (iconBytes != null && iconBytes.length > 0) {
        	
        	String md5Hex = MD5Util.md5Hex(iconBytes);
        	Bitmap cachedBM = null;
        	if (md5Hex != null)
        	{
        		cachedBM = EntriesCursorAdapter.mMemoryCache.get(md5Hex);
        		if (cachedBM == null)
        		{
        			cachedBM = BitmapFactory.decodeByteArray(iconBytes, 0, iconBytes.length);
					if (cachedBM != null) {
						int bitmapSizeInDip = UiUtils.dpToPixel(18);
						if (cachedBM.getHeight() != bitmapSizeInDip) {
							cachedBM = Bitmap
									.createScaledBitmap(cachedBM,
											bitmapSizeInDip,
											bitmapSizeInDip, false);
						}
						EntriesCursorAdapter.mMemoryCache.put(md5Hex, cachedBM);
					}
        		}
        	}
        	if (cachedBM != null)
        	{
        		textView.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(context.getResources(), cachedBM), null, null, null);
        	} else {
        		textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        	}
        } else {
            view.setTag(null);
            textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }

        textView.setText((cursor.isNull(namePosition) ? cursor.getString(linkPosition) : cursor.getString(namePosition)));
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        ImageView indicatorImage = (ImageView) view.findViewById(R.id.indicator);

        if (cursor.getInt(isGroupPosition) == 1) {
            indicatorImage.setVisibility(View.VISIBLE);

            TextView textView = ((TextView) view.findViewById(android.R.id.text1));
            textView.setEnabled(true);
            textView.setText(cursor.getString(namePosition));
            textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            textView.setText(cursor.getString(namePosition));

            if (isExpanded)
                indicatorImage.setImageResource(R.drawable.group_expanded);
            else
                indicatorImage.setImageResource(R.drawable.group_collapsed);
        } else {
            bindChildView(view, context, cursor);
            indicatorImage.setVisibility(View.GONE);
        }
    }

    @Override
    protected Uri getChildrenUri(Cursor groupCursor) {
        return FeedColumns.FEEDS_FOR_GROUPS_CONTENT_URI(groupCursor.getLong(idPosition));
    }

    @Override
    public void notifyDataSetChanged() {
        getCursorPositions(null);
        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged(Cursor data) {
        getCursorPositions(data);
    }

    @Override
    public void notifyDataSetInvalidated() {
        getCursorPositions(null);
        super.notifyDataSetInvalidated();
    }

    private synchronized void getCursorPositions(Cursor cursor) {
        if (cursor != null && isGroupPosition == -1) {
            isGroupPosition = cursor.getColumnIndex(FeedColumns.IS_GROUP);
            namePosition = cursor.getColumnIndex(FeedColumns.NAME);
            idPosition = cursor.getColumnIndex(FeedColumns._ID);
            linkPosition = cursor.getColumnIndex(FeedColumns.URL);
            iconPosition = cursor.getColumnIndex(FeedColumns.ICON);
        }
    }
}
