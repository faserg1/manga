package com.danilov.supermanga.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.httpimage.HttpImageManager;
import com.danilov.supermanga.DaggerApplicationComponent;
import com.danilov.supermanga.R;
import com.danilov.supermanga.activity.MangaInfoActivity;
import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.http.ExtendedHttpClient;
import com.danilov.supermanga.core.model.LocalManga;
import com.danilov.supermanga.core.model.Manga;
import com.danilov.supermanga.core.repository.RepositoryEngine;
import com.danilov.supermanga.core.repository.special.JavaScriptEngine;
import com.danilov.supermanga.core.service.LocalImageManager;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.core.util.IoUtils;
import com.danilov.supermanga.core.util.Utils;
import com.danilov.supermanga.core.view.ScrollViewParallax;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.ButterKnife;

/**
 * Created by Semyon on 29.02.2016.
 */
public class WorldArtFragment extends BaseFragmentNative {

    @Bind(R.id.manga_title)
    public TextView mangaTitle;

    @Bind(R.id.world_art_toolbar)
    public View worldArtToolbar;

    @Bind(R.id.manga_description)
    public TextView mangaDescriptionTextView;

    @Bind(R.id.chapters_quantity)
    public TextView chaptersQuantityTextView;

    @Bind(R.id.authors)
    public TextView mangaAuthor;

    @Bind(R.id.genres)
    public TextView mangaGenres;

    @Bind(R.id.manga_cover)
    public ImageView mangaCover;

    @Bind(R.id.scrollView)
    public ScrollViewParallax scrollViewParallax;

    @Bind(R.id.manga_images)
    public RecyclerView mangaImagesView;

    @BindDimen(R.dimen.grid_item_height)
    public int sizeOfImage;

    @Inject
    public LocalImageManager localImageManager;

    @Inject
    public HttpImageManager httpImageManager;

    private boolean isBackPressed = false;

    public static WorldArtFragment newInstance(final Manga manga) {
        WorldArtFragment fragment = new WorldArtFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.MANGA_PARCEL_KEY, manga);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.world_art_activity, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ButterKnife.bind(this, view);
        MangaApplication.get().applicationComponent().inject(this);

        final float size = getResources().getDimension(R.dimen.info_parallax_image_height);
        final int baseColor = getResources().getColor(R.color.color_world_art);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            worldArtToolbar.setAlpha(0);
        } else {
            worldArtToolbar.setVisibility(View.INVISIBLE);
        }
        scrollViewParallax.setScrollListener((horizontal, vertical, oldl, oldt) -> {
            float alpha = 1 - (float) Math.max(0, size - vertical) / size;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                worldArtToolbar.setAlpha(alpha);
            } else {
                if (alpha > 0.5) {
                    worldArtToolbar.setVisibility(View.VISIBLE);
                }
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        mangaImagesView.setLayoutManager(layoutManager);
        mangaImagesView.setMinimumWidth(150);
        mangaImagesView.addItemDecoration(new RecyclerView.ItemDecoration() {

            @Override
            public void getItemOffsets(final Rect outRect, final View view, final RecyclerView parent, final RecyclerView.State state) {
                outRect.right = 20;
            }

        });
        ((MangaInfoActivity) getActivity()).getToolbar().setVisibility(View.INVISIBLE);
        testInit();
    }

    private void testInit() {
        final Manga manga = getArguments().getParcelable(Constants.MANGA_PARCEL_KEY);
        final MangaInfoActivity activity = (MangaInfoActivity) getActivity();
        activity.startRefresh();
        new Thread() {
            @Override
            public void run() {
                String uri = null;
                try {
                    uri = "http://www.world-art.ru/search.php?public_search=" + URLEncoder.encode(manga.getTitle(), Charset.forName(HTTP.UTF_8).name()) + "&global_sector=manga";
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                HttpGet request = new HttpGet(uri);

                HttpContext context = new BasicHttpContext();
                HttpClient httpClient = new ExtendedHttpClient();
                HttpResponse response = null;
                try {
                    response = httpClient.execute(request, context);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                byte[] result = new byte[0];
                try {
                    result = IoUtils.convertStreamToBytes(response.getEntity().getContent());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String responseString = IoUtils.convertBytesToString(result);

                String mangaCoverUrl = "";

                if (responseString.startsWith("<meta http-equiv=")) {
                    String url = responseString.substring(responseString.indexOf("url=") + 4);
                    url = url.substring(0, url.indexOf("'>"));
                    url = "http://www.world-art.ru" + url;
                    request = new HttpGet(url);

                    context = new BasicHttpContext();
                    httpClient = new ExtendedHttpClient();
                    response = null;
                    try {
                        response = httpClient.execute(request, context);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    result = new byte[0];
                    try {
                        result = IoUtils.convertStreamToBytes(response.getEntity().getContent());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    responseString = IoUtils.convertBytesToString(result);
                }

                final List<String> imagesList = new ArrayList<String>();

                JavaScriptEngine.DocHelper docHelper = new JavaScriptEngine.DocHelper(responseString);
                JavaScriptEngine.ElementsHelper img = docHelper.select("body table td:nth-child(1) > img");
                mangaCoverUrl = "http://www.world-art.ru/animation/" + img.attr("src");
                JavaScriptEngine.ElementsHelper imgs = docHelper.select("noindex img");
                int size = imgs.size();
                for (int i = 0; i < 10 && i < size - 1; i++) {
                    String image = "http://www.world-art.ru/animation/" + imgs.get(i).attr("src");
                    imagesList.add(image);
                }
                final String cov = mangaCoverUrl;
                activity.runOnUiThread(() -> {
                    Uri coverUri = Uri.parse(cov);
                    HttpImageManager.LoadRequest _request = HttpImageManager.LoadRequest.obtain(coverUri,
                            mangaCover, null, sizeOfImage);
                    Bitmap bitmap = httpImageManager.loadImage(_request);
                    if (bitmap != null) {
                        mangaCover.setImageBitmap(bitmap);
                    }
                    mangaImagesView.setAdapter(new ImagesAdapter(imagesList));
                    activity.stopRefresh();
                    ((MangaInfoActivity) getActivity()).getToolbar().setVisibility(View.INVISIBLE);
                });
            }
        }.start();


        mangaTitle.setText(manga.getTitle());
        mangaGenres.setText(manga.getGenres());
        mangaAuthor.setText(manga.getAuthor());
        mangaDescriptionTextView.setText(manga.getDescription());

        if (manga.isDownloaded()) {
            LocalManga localManga = (LocalManga) manga;
            String mangaUri = localManga.getLocalUri();
            Bitmap bitmap = localImageManager.loadBitmap(mangaCover, mangaUri + "/cover", sizeOfImage);
            if (bitmap != null) {
                mangaCover.setImageBitmap(bitmap);
            }
        } else {
            if (manga.getCoverUri() != null) {
                Uri coverUri = Uri.parse(manga.getCoverUri());
                HttpImageManager.LoadRequest request = HttpImageManager.LoadRequest.obtain(coverUri, mangaCover, manga.getRepository().getEngine().getRequestPreprocessor(), sizeOfImage);
                Bitmap bitmap = httpImageManager.loadImage(request);
                if (bitmap != null) {
                    mangaCover.setImageBitmap(bitmap);
                }
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        if (isBackPressed) {
            return false;
        }
        isBackPressed = true;
        ((MangaInfoActivity) getActivity()).getToolbar().setVisibility(View.VISIBLE);
        return false;
    }

    private class ImagesAdapter extends RecyclerView.Adapter<ImageHolder> {

        private List<String> imageIds;

        private Context context = applicationContext;

        public ImagesAdapter(final List<String> imageIds) {
            this.imageIds = imageIds;
        }

        @Override
        public ImageHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.manga_image_screenshot_small, parent, false);
//            v.setOnClickListener(WorldArtActivity.this);
            return new ImageHolder(v);
        }

        @Override
        public void onBindViewHolder(final ImageHolder holder, final int position) {
            String imgUrl = imageIds.get(position);

            Uri coverUri = Uri.parse(imgUrl);
            HttpImageManager.LoadRequest request = HttpImageManager.LoadRequest.obtain(coverUri, holder.mangaScreenSmall, null, sizeOfImage);
            Bitmap bitmap = httpImageManager.loadImage(request);
            if (bitmap != null) {
                holder.mangaScreenSmall.setImageBitmap(bitmap);
            }
        }

        public List<String> getImageIds() {
            return imageIds;
        }

        @Override
        public int getItemCount() {
            return imageIds.size();
        }

    }

    private class ImageHolder extends RecyclerView.ViewHolder {

        ImageView mangaScreenSmall;

        public ImageHolder(final View itemView) {
            super(itemView);
            mangaScreenSmall = (ImageView) itemView;
        }
    }

    public class OnSwipeTouchListener implements View.OnTouchListener {

        private GestureDetector gestureDetector;

        public OnSwipeTouchListener(Context c) {
            gestureDetector = new GestureDetector(c, new GestureListener());
        }

        public boolean onTouch(final View view, final MotionEvent motionEvent) {
            return gestureDetector.onTouchEvent(motionEvent);
        }

        public void onSwipeRight() {
        }

        public void onSwipeLeft() {
        }

        public void onSwipeUp() {
        }

        public void onSwipeDown() {
        }

        public void onClick() {

        }

        public void onDoubleClick() {

        }

        public void onLongClick() {

        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                onClick();
                return super.onSingleTapUp(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                onDoubleClick();
                return super.onDoubleTap(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                onLongClick();
                super.onLongPress(e);
            }

            // Determines the fling velocity and then fires the appropriate swipe event accordingly
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                        }
                    } else {
                        if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffY > 0) {
                                onSwipeDown();
                            } else {
                                onSwipeUp();
                            }
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }
    }

}
