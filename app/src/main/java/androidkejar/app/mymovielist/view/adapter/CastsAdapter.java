package androidkejar.app.mymovielist.view.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import androidkejar.app.mymovielist.R;
import androidkejar.app.mymovielist.controller.MoviesURL;
import androidkejar.app.mymovielist.pojo.ItemObject;

/**
 * Created by alodokter-it on 12/11/16.
 */

public class CastsAdapter extends RecyclerView.Adapter<CastsAdapter.ListHolder> {

    Context context;
    List<ItemObject.Credits.Cast> itemObjects;

    public CastsAdapter(Context context, List<ItemObject.Credits.Cast> itemObjects) {
        this.context = context;
        this.itemObjects = itemObjects;
    }

    @Override
    public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.detail_movie_casts_cardview, null);
        return new ListHolder(view);
    }

    @Override
    public void onBindViewHolder(final ListHolder holder, int position) {
        holder.detailCastsName.setText(itemObjects.get(position).getName());
        holder.detailCastsCharacter.setText(itemObjects.get(position).getCharacter());
        Glide.with(context)
                .load(MoviesURL.getUrlImage(itemObjects.get(position).getProfilePath()))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .centerCrop()
                .into(holder.detailCastsPic);
    }

    @Override
    public int getItemCount() {
        return itemObjects.size();
    }

    class ListHolder extends RecyclerView.ViewHolder {
        TextView detailCastsName;
        TextView detailCastsCharacter;
        ImageView detailCastsPic;
        CardView detailCastsLayout;

        ListHolder(View itemView) {
            super(itemView);
            detailCastsName = (TextView) itemView.findViewById(R.id.detail_casts_name);
            detailCastsCharacter = (TextView) itemView.findViewById(R.id.detail_casts_character);
            detailCastsPic = (ImageView) itemView.findViewById(R.id.detail_casts_pic);
            detailCastsLayout = (CardView) itemView.findViewById(R.id.detail_casts_layout);
        }
    }
}
