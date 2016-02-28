package com.danilov.supermanga.activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.util.Utils;
import com.danilov.supermanga.core.view.ScrollViewParallax;
import com.danilov.supermanga.fragment.BaseFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Semyon on 28.02.2016.
 */
public class WorldArtActivity extends BaseToolbarActivity {

    private TextView mangaTitle;

    private View worldArtToolbar;

    private TextView mangaDescriptionTextView;

    private TextView chaptersQuantityTextView;

    private TextView mangaAuthor;

    private TextView mangaGenres;

    private ImageView mangaCover = null;

    private ScrollViewParallax scrollViewParallax;

    private RecyclerView mangaImagesView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.world_art_activity);
        scrollViewParallax = findViewWithId(R.id.scrollView);
        mangaDescriptionTextView = findViewWithId(R.id.manga_description);
        chaptersQuantityTextView = findViewWithId(R.id.chapters_quantity);
        mangaAuthor = findViewWithId(R.id.authors);
        mangaGenres = findViewWithId(R.id.genres);
        mangaTitle = findViewWithId(R.id.manga_title);
        mangaCover = findViewWithId(R.id.manga_cover);
        worldArtToolbar = findViewWithId(R.id.world_art_toolbar);


        final float size = getResources().getDimension(R.dimen.info_parallax_image_height);
        final int baseColor = getResources().getColor(R.color.color_world_art);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            worldArtToolbar.setAlpha(0);
        } else {
            worldArtToolbar.setVisibility(View.INVISIBLE);
        }
        scrollViewParallax.setScrollListener(new ScrollViewParallax.ScrollListener() {
            @Override
            public void onScroll(final int horizontal, final int vertical, final int oldl, final int oldt) {
                float alpha = 1 - (float) Math.max(0, size - vertical) / size;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    worldArtToolbar.setAlpha(alpha);
                } else {
                    if (alpha > 0.5) {
                        worldArtToolbar.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        LinearLayoutManager layoutManager= new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false);
        mangaImagesView = findViewWithId(R.id.manga_images);
        mangaImagesView.setLayoutManager(layoutManager);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        testInit();
    }

    private void testInit() {
        mangaTitle.setText("God Eater: The Spiral Fate");
        mangaGenres.setText("приключения, фэнтези, сёнэн");
        mangaAuthor.setText("Сайто Рокуро");
        mangaDescriptionTextView.setText("Действия происходят в 2071 году, на Земле появились монстры «Арагами» и начали охоту на людей. Всего за несколько лет человеческая популяция сократилась до одной сотой, а планета превратилась в бесконечную пустыню. Но нашлась группа людей, борющихся с монстрами с помощью биологического оружия «Джинки». Их назвали «Пожирателями богов». Смогут ли они прекратить завоевание и вернуть человечеству надежду на будущее?");
        mangaCover.setImageResource(R.drawable.test_cover1);
        mangaImagesView.setAdapter(new ImagesAdapter(new ArrayList<Integer>(Arrays.asList(R.drawable.test_screen0, R.drawable.test_screen1, R.drawable.test_screen2, R.drawable.test_screen3, R.drawable.test_screen4))));
    }

    private class ImagesAdapter extends RecyclerView.Adapter<ImageHolder> {

        private List<Integer> imageIds;

        private Context context = getApplicationContext();

        public ImagesAdapter(final List<Integer> imageIds) {
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
            Integer imageId = imageIds.get(position);
            holder.mangaScreenSmall.setImageResource(imageId);
        }

        public List<Integer> getImageIds() {
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

}