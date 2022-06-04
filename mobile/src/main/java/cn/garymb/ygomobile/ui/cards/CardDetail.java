package cn.garymb.ygomobile.ui.cards;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bm.library.PhotoView;
import com.feihua.dialogutils.util.DialogUtils;

import java.io.File;

import cn.garymb.ygomobile.AppsSettings;
import cn.garymb.ygomobile.Constants;
import cn.garymb.ygomobile.core.IrrlichtBridge;
import cn.garymb.ygomobile.lite.R;
import cn.garymb.ygomobile.loader.ImageLoader;
import cn.garymb.ygomobile.ui.activities.BaseActivity;
import cn.garymb.ygomobile.ui.adapters.BaseAdapterPlus;
import cn.garymb.ygomobile.utils.CardUtils;
import cn.garymb.ygomobile.utils.DownloadUtil;
import cn.garymb.ygomobile.utils.FileUtils;
import cn.garymb.ygomobile.utils.YGOUtil;
import ocgcore.CardManager;
import ocgcore.DataManager;
import ocgcore.StringManager;
import ocgcore.data.Card;
import ocgcore.enums.CardType;

import static cn.garymb.ygomobile.core.IrrlichtBridge.ACTION_SHARE_FILE;

/***
 * 卡片详情
 */
public class CardDetail extends BaseAdapterPlus.BaseViewHolder {

    private static final int TYPE_DOWNLOAD_CARD_IMAGE_OK = 0;
    private static final int TYPE_DOWNLOAD_CARD_IMAGE_EXCEPTION = 1;
    private static final int TYPE_DOWNLOAD_CARD_IMAGE_ING = 2;

    private static final String TAG = "CardDetail";
    private final CardManager cardManager;
    private final ImageView cardImage;
    private final TextView name;
    private final TextView desc;
    private final TextView level;
    private final TextView type;
    private final TextView race;
    private final TextView cardAtk;
    private final TextView cardDef;

    private final TextView setName;
    private final TextView otView;
    private final TextView attrView;
    private final View monsterLayout;
    private final View close;
    private final View faq;
    private final View addMain;
    private final View addSide;
    private final View linkArrow;
    private final View layoutDetailPScale;
    private final TextView detailCardScale;
    private final TextView cardCode;
    private final View lbSetCode;
    private final ImageLoader imageLoader;
    private final View mImageFav, atkdefView;

    private final StringManager mStringManager;
    private int curPosition;
    private Card mCardInfo;
    private CardListProvider mProvider;
    private OnCardClickListener mListener;
    private DialogUtils dialog;
    private PhotoView photoView;
    private LinearLayout ll_bar;
    private ProgressBar pb_loading;
    private TextView tv_loading;
    private LinearLayout ll_btn;
    private Button btn_redownload;
    private Button btn_share;
    private boolean isDownloadCardImage = true;
    private boolean mShowAdd = false;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TYPE_DOWNLOAD_CARD_IMAGE_OK:
                    isDownloadCardImage = true;
                    ll_bar.startAnimation(AnimationUtils.loadAnimation(context, R.anim.out_from_bottom));
                    ll_bar.setVisibility(View.GONE);
                    imageLoader.bindImage(photoView, msg.arg1, ImageLoader.Type.origin);
                    imageLoader.bindImage(cardImage, msg.arg1, ImageLoader.Type.middle);
                    if (mListener != null) {
                        mListener.onImageUpdate(mCardInfo);
                    }
                    break;
                case TYPE_DOWNLOAD_CARD_IMAGE_ING:
                    tv_loading.setText(msg.arg1 + "%");
                    pb_loading.setProgress(msg.arg1);
                    break;
                case TYPE_DOWNLOAD_CARD_IMAGE_EXCEPTION:
                    isDownloadCardImage = true;
                    ll_bar.startAnimation(AnimationUtils.loadAnimation(context, R.anim.out_from_bottom));
                    ll_bar.setVisibility(View.GONE);
                    YGOUtil.show("error" + msg.obj);
                    break;

            }
        }
    };
    private final BaseActivity mContext;
    private OnFavoriteChangedListener mCallBack;

    public CardDetail(BaseActivity context, ImageLoader imageLoader, StringManager stringManager) {
        super(context.getLayoutInflater().inflate(R.layout.dialog_cardinfo, null));
        mContext = context;
        cardImage = findViewById(R.id.card_image);
        this.imageLoader = imageLoader;
        mStringManager = stringManager;
        name = findViewById(R.id.text_name);
        desc = findViewById(R.id.text_desc);
        close = findViewById(R.id.btn_close);
        cardCode = findViewById(R.id.card_code);
        level = findViewById(R.id.card_level);
        linkArrow = findViewById(R.id.detail_link_arrows);
        type = findViewById(R.id.card_type);
        faq = findViewById(R.id.btn_faq);
        cardAtk = findViewById(R.id.card_atk);
        cardDef = findViewById(R.id.card_def);
        atkdefView = findViewById(R.id.layout_atkdef2);
        mImageFav = findViewById(R.id.image_fav);

        monsterLayout = findViewById(R.id.layout_monster);
        layoutDetailPScale = findViewById(R.id.detail_p_scale);
        detailCardScale = findViewById(R.id.detail_cardscale);
        race = findViewById(R.id.card_race);
        setName = findViewById(R.id.card_setname);
        addMain = findViewById(R.id.btn_add_main);
        addSide = findViewById(R.id.btn_add_side);
        otView = findViewById(R.id.card_ot);
        attrView = findViewById(R.id.card_attribute);
        lbSetCode = findViewById(R.id.label_setcode);
        cardManager = DataManager.get().getCardManager();
        close.setOnClickListener((v) -> {
            if (mListener != null) {
                mListener.onClose();
            }
        });
        addMain.setOnClickListener((v) -> {
            if (mListener != null) {
                Card cardInfo = getCardInfo();
                if (cardInfo == null) {
                    return;
                }
                mListener.onAddMainCard(cardInfo);
            }
        });
        addSide.setOnClickListener((v) -> {
            if (mListener != null) {
                Card cardInfo = getCardInfo();
                if (cardInfo == null) {
                    return;
                }
                mListener.onAddSideCard(cardInfo);
            }
        });
        faq.setOnClickListener((v) -> {
            if (mListener != null) {
                Card cardInfo = getCardInfo();
                if (cardInfo == null) {
                    return;
                }
                mListener.onOpenUrl(cardInfo);
            }
        });
        findViewById(R.id.lastone).setOnClickListener((v) -> {
            onPreCard();
        });
        findViewById(R.id.nextone).setOnClickListener((v) -> {
            onNextCard();
        });
        mImageFav.setOnClickListener((v) -> {
            doMyFavorites(getCardInfo());
        });
    }

    /**
     * 收藏卡片
     */
    public void doMyFavorites(Card cardInfo) {
        boolean ret = CardFavorites.get().toggle(cardInfo.Code);
        mImageFav.setSelected(ret);
        if (mCallBack != null) {
            mCallBack.onFavoriteChange(cardInfo, ret);
        }
    }

    public ImageView getCardImage() {
        return cardImage;
    }

    public void hideClose() {
        close.setVisibility(View.GONE);
    }

    public void showAdd() {
        mShowAdd = true;
        addSide.setVisibility(View.VISIBLE);
        addMain.setVisibility(View.VISIBLE);
    }

    public View getView() {
        return view;
    }

    public BaseActivity getContext() {
        return mContext;
    }

    public void setOnCardClickListener(OnCardClickListener listener) {
        mListener = listener;
    }

    public void setCallBack(OnFavoriteChangedListener callBack) {
        mCallBack = callBack;
    }

    public void bind(Card cardInfo, final int position, final CardListProvider provider) {
        curPosition = position;
        mProvider = provider;
        if (cardInfo != null) {
            setCardInfo(cardInfo, view);
        }
    }

    public int getCurPosition() {
        return curPosition;
    }

    public CardListProvider getProvider() {
        return mProvider;
    }

    public Card getCardInfo() {
        return mCardInfo;
    }

    private void setCardInfo(Card cardInfo, View view) {
        if (cardInfo == null) return;
        mCardInfo = cardInfo;
        imageLoader.bindImage(cardImage, cardInfo, ImageLoader.Type.middle);
        dialog = DialogUtils.getdx(context);
        cardImage.setOnClickListener((v) -> {
            showCardImageDetail(cardInfo.Code);
        });
        name.setText(cardInfo.Name);
        desc.setText(cardInfo.Desc);
        cardCode.setText(String.format("%08d", cardInfo.getCode()));
        if (cardInfo.isType(CardType.Token)) {
            faq.setVisibility(View.INVISIBLE);
        } else {
            faq.setVisibility(View.VISIBLE);
        }
        if (mShowAdd) {
            if (cardInfo.isType(CardType.Token)) {
                addSide.setVisibility(View.INVISIBLE);
                addMain.setVisibility(View.INVISIBLE);
            } else {
                addSide.setVisibility(View.VISIBLE);
                addMain.setVisibility(View.VISIBLE);
            }
        }
        //按是否存在于收藏夹切换显示图标
        mImageFav.setSelected(CardFavorites.get().hasCard(cardInfo.Code));

        type.setText(CardUtils.getAllTypeString(cardInfo, mStringManager).replace("/", "|"));
        attrView.setText(mStringManager.getAttributeString(cardInfo.Attribute));
        otView.setText(mStringManager.getOtString(cardInfo.Ot, true));
        long[] sets = cardInfo.getSetCode();
        setName.setText("");
        int index = 0;
        for (long set : sets) {
            if (set > 0) {
                if (index != 0) {
                    setName.append("\n");
                }
                setName.append("" + mStringManager.getSetName(set));
                index++;
            }
        }

        if (TextUtils.isEmpty(setName.getText())) {
            setName.setVisibility(View.INVISIBLE);
            lbSetCode.setVisibility(View.INVISIBLE);
        } else {
            setName.setVisibility(View.VISIBLE);
            lbSetCode.setVisibility(View.VISIBLE);
        }

        if (cardInfo.isType(CardType.Monster)) {
            atkdefView.setVisibility(View.VISIBLE);
            monsterLayout.setVisibility(View.VISIBLE);
            race.setVisibility(View.VISIBLE);
            String star = "★" + cardInfo.getStar();
           /* for (int i = 0; i < cardInfo.getStar(); i++) {
                star += "★";
            }*/
            level.setText(star);
            if (cardInfo.isType(CardType.Xyz)) {
                level.setTextColor(context.getResources().getColor(R.color.star_rank));
            } else {
                level.setTextColor(context.getResources().getColor(R.color.star));
            }
            if (cardInfo.isType(CardType.Pendulum)) {
                layoutDetailPScale.setVisibility(View.VISIBLE);
                detailCardScale.setText(String.valueOf(cardInfo.LeftScale));
            } else {
                layoutDetailPScale.setVisibility(View.GONE);
            }
            cardAtk.setText((cardInfo.Attack < 0 ? "?" : String.valueOf(cardInfo.Attack)));
            //连接怪兽设置
            if (cardInfo.isType(CardType.Link)) {
                level.setVisibility(View.GONE);
                linkArrow.setVisibility(View.VISIBLE);
                cardDef.setText((cardInfo.getStar() < 0 ? "?" : "LINK-" + String.valueOf(cardInfo.getStar())));
                BaseActivity.showLinkArrows(cardInfo, view);
            } else {
                level.setVisibility(View.VISIBLE);
                linkArrow.setVisibility(View.GONE);
                cardDef.setText((cardInfo.Defense < 0 ? "?" : String.valueOf(cardInfo.Defense)));
            }
            race.setText(mStringManager.getRaceString(cardInfo.Race));
        } else {
            atkdefView.setVisibility(View.GONE);
            race.setVisibility(View.GONE);
            monsterLayout.setVisibility(View.GONE);
            level.setVisibility(View.GONE);
            linkArrow.setVisibility(View.GONE);
        }
    }

    private void showCardImageDetail(int code) {
        AppsSettings appsSettings = AppsSettings.get();
        View view = dialog.initDialog(context, R.layout.dialog_photo);

        dialog.setDialogWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        Window dialogWindow = dialog.getDialog().getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setAttributes(lp);

        photoView = view.findViewById(R.id.photoView);
        ll_bar = view.findViewById(R.id.ll_bar);
        pb_loading = view.findViewById(R.id.pb_loading);
        tv_loading = view.findViewById(R.id.tv_name);
        pb_loading.setMax(100);

        ll_btn = view.findViewById(R.id.ll_btn);
        btn_redownload = view.findViewById(R.id.btn_redownload);
        btn_share = view.findViewById(R.id.btn_share);

        // 启用图片缩放功能
        photoView.enable();

        photoView.setOnClickListener(View -> {
            if (ll_btn.getVisibility() == View.VISIBLE) {
                ll_btn.startAnimation(AnimationUtils.loadAnimation(context, R.anim.push_out));
                ll_btn.setVisibility(View.GONE);
            } else {
                dialog.dis();
            }
        });

        photoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isDownloadCardImage || cardManager.getCard(code) == null)
                    return false;
                ll_btn.startAnimation(AnimationUtils.loadAnimation(context, R.anim.push_in));
                ll_btn.setVisibility(View.VISIBLE);
                btn_redownload.setOnClickListener((s) -> {
                    ll_btn.startAnimation(AnimationUtils.loadAnimation(context, R.anim.push_out));
                    ll_btn.setVisibility(View.GONE);
                    downloadCardImage(code, true);
                });

                btn_share.setOnClickListener((s) -> {
                    ll_btn.startAnimation(AnimationUtils.loadAnimation(context, R.anim.push_out));
                    ll_btn.setVisibility(View.GONE);
                    String fname = String.valueOf(code);
                    Intent intent = new Intent(ACTION_SHARE_FILE);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.putExtra(IrrlichtBridge.EXTRA_SHARE_TYPE, "jpg");
                    intent.putExtra(IrrlichtBridge.EXTRA_SHARE_FILE, fname + Constants.IMAGE_URL_EX);
                    intent.setPackage(context.getPackageName());
                    try {
                        context.startActivity(intent);
                    } catch (Throwable e) {
                        Toast.makeText(context, "dev error:not found activity.", Toast.LENGTH_SHORT).show();
                    }
                });

                imageLoader.bindImage(cardImage, code, null, ImageLoader.Type.origin);
                return true;
            }
        });

        //先显示普通卡片大图，判断如果没有高清图就下载
        imageLoader.bindImage(photoView, code, null, ImageLoader.Type.middle);

        if (null == ImageLoader.getImageFile(code)) {
            //downloadCardImage(code, false);
        }

    }

    private void downloadCardImage(int code, boolean force) {
        if (cardManager.getCard(code) == null) {
            YGOUtil.show(context.getString(R.string.tip_expansions_image));
            return;
        }
        File imgFile = new File(AppsSettings.get().getCardImagePath(code));
        final File tmp = new File(imgFile.getAbsolutePath() + ".tmp");
        if (tmp.exists()) {
            if (force) {
                //强制下载，就删除tmp,重新下载
                FileUtils.deleteFile(tmp);
                //删除原来卡图
                FileUtils.deleteFile(imgFile);
            } else {
                return;
            }
        }
        isDownloadCardImage = false;
        ll_bar.setVisibility(View.VISIBLE);
        ll_bar.startAnimation(AnimationUtils.loadAnimation(context, R.anim.in_from_top));
        DownloadUtil.get().download(YGOUtil.getCardImageDetailUrl(code), tmp.getParent(), tmp.getName(), new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(File file) {
                boolean bad = file.length() < 50 * 1024;
                if (bad || !tmp.renameTo(imgFile)) {
                    FileUtils.deleteFile(file);
                    Message message = new Message();
                    message.what = TYPE_DOWNLOAD_CARD_IMAGE_EXCEPTION;
                    message.obj = context.getString(R.string.download_image_error);
                    handler.sendMessage(message);
                } else {
                    Message message = new Message();
                    message.what = TYPE_DOWNLOAD_CARD_IMAGE_OK;
                    message.arg1 = code;
                    handler.sendMessage(message);
                }
            }

            @Override
            public void onDownloading(int progress) {
                Message message = new Message();
                message.what = TYPE_DOWNLOAD_CARD_IMAGE_ING;
                message.arg1 = progress;
                handler.sendMessage(message);
            }

            @Override
            public void onDownloadFailed(Exception e) {
                Log.w(IrrlichtBridge.TAG, "download image error:" + e.getMessage());
                //下载失败后删除下载的文件
                FileUtils.deleteFile(tmp);
//                downloadCardImage(code, file);

                Message message = new Message();
                message.what = TYPE_DOWNLOAD_CARD_IMAGE_EXCEPTION;
                message.obj = e.toString();
                handler.sendMessage(message);
            }
        });

    }

    public void onPreCard() {
        int position = getCurPosition();
        CardListProvider provider = getProvider();
        if (position == 0) {
            getContext().showToast(R.string.already_top, Toast.LENGTH_SHORT);
        } else {
            int index = position;
            do {
                if (index == 0) {
                    getContext().showToast(R.string.already_top, Toast.LENGTH_SHORT);
                    return;
                } else {
                    index--;
                }
            } while (provider.getCard(index) == null || provider.getCard(index).Name == null || provider.getCard(position).Name.equals(provider.getCard(index).Name));

            bind(provider.getCard(index), index, provider);
            if (position == 1) {
                getContext().showToast(R.string.already_top, Toast.LENGTH_SHORT);
            }
        }
    }

    public void onNextCard() {
        int position = getCurPosition();
        CardListProvider provider = getProvider();
        if (position < provider.getCardsCount() - 1) {
            int index = position;
            do {
                if (index == provider.getCardsCount() - 1) {
                    getContext().showToast(R.string.already_end, Toast.LENGTH_SHORT);
                    return;
                } else {
                    index++;
                }
            } while (provider.getCard(index) == null || provider.getCard(index).Name == null || provider.getCard(position).Name.equals(provider.getCard(index).Name));

            bind(provider.getCard(index), index, provider);
            if (position == provider.getCardsCount() - 1) {
                getContext().showToast(R.string.already_end, Toast.LENGTH_SHORT);
            }
        } else {
            getContext().showToast(R.string.already_end, Toast.LENGTH_SHORT);
        }
    }


    public interface OnFavoriteChangedListener {
        void onFavoriteChange(Card card, boolean favorite);
    }

    public interface OnCardClickListener {
        void onOpenUrl(Card cardInfo);

        void onAddMainCard(Card cardInfo);

        void onAddSideCard(Card cardInfo);


        void onImageUpdate(Card cardInfo);


        void onClose();
    }

    public static class DefaultOnCardClickListener implements OnCardClickListener {
        public DefaultOnCardClickListener() {
        }

        @Override
        public void onOpenUrl(Card cardInfo) {

        }

        @Override
        public void onClose() {
        }

        @Override
        public void onImageUpdate(Card cardInfo) {

        }

        @Override
        public void onAddSideCard(Card cardInfo) {

        }

        @Override
        public void onAddMainCard(Card cardInfo) {

        }
    }

}
