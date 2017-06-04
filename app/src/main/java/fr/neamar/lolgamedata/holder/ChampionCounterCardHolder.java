package fr.neamar.lolgamedata.holder;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import fr.neamar.lolgamedata.ChampionActivity;
import fr.neamar.lolgamedata.R;
import fr.neamar.lolgamedata.pojo.ChampionCounter;

public class ChampionCounterCardHolder extends DummyHolder implements View.OnClickListener {
        private final ImageView championImage;
        private final TextView championNameText;
        private final TextView winrateText;
        private final TextView disclaimer;

        private ChampionCounter championCounter = null;

        public ChampionCounterCardHolder(View itemView) {
            super(itemView);
            championImage = (ImageView) itemView.findViewById(R.id.championImage);
            championNameText = (TextView) itemView.findViewById(R.id.championNameText);
            winrateText = (TextView) itemView.findViewById(R.id.winrateText);
            disclaimer = (TextView) itemView.findViewById(R.id.disclaimer);

            itemView.setOnClickListener(this);
        }

        public void bindChampion(ChampionCounter champion) {
            this.championCounter = champion;
            ImageLoader.getInstance().displayImage(champion.image, championImage);
            championNameText.setText(champion.name);
            winrateText.setText(String.format(winrateText.getContext().getString(R.string.s_winrate), champion.winRate + "%"));
            disclaimer.setText(String.format(disclaimer.getContext().getString(R.string.patch_s), champion.patch));
        }

    @Override
    public void onClick(View view) {
        if(championCounter != null) {
            Intent detailIntent = new Intent(view.getContext(), ChampionActivity.class);
            detailIntent.putExtra("championName", championCounter.name);
            detailIntent.putExtra("championId", championCounter.id);
            detailIntent.putExtra("from", "counter");

            view.getContext().startActivity(detailIntent);
        }
    }
}