package cn.garymb.ygomobile.ui.home;


import static cn.garymb.ygomobile.Constants.ASSET_SERVER_LIST;
import static cn.garymb.ygomobile.utils.DownloadUtil.TYPE_DOWNLOAD_EXCEPTION;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.app.hubert.guide.NewbieGuide;
import com.app.hubert.guide.core.Controller;
import com.app.hubert.guide.listener.OnHighlightDrewListener;
import com.app.hubert.guide.listener.OnLayoutInflatedListener;
import com.app.hubert.guide.model.GuidePage;
import com.app.hubert.guide.model.HighLight;
import com.app.hubert.guide.model.HighlightOptions;
import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.BoomButtons.TextOutsideCircleButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.ourygo.assistant.base.listener.OnDuelAssistantListener;
import com.ourygo.assistant.util.DuelAssistantManagement;
import com.ourygo.assistant.util.Util;
import com.tencent.bugly.beta.Beta;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.QbSdk;
import com.tubb.smrv.SwipeMenuRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import cn.garymb.ygodata.YGOGameOptions;
import cn.garymb.ygomobile.AppsSettings;
import cn.garymb.ygomobile.Constants;
import cn.garymb.ygomobile.YGOMobileActivity;
import cn.garymb.ygomobile.YGOStarter;
import cn.garymb.ygomobile.bean.Deck;
import cn.garymb.ygomobile.bean.ServerInfo;
import cn.garymb.ygomobile.bean.ServerList;
import cn.garymb.ygomobile.bean.events.ServerInfoEvent;
import cn.garymb.ygomobile.lite.BuildConfig;
import cn.garymb.ygomobile.lite.R;
import cn.garymb.ygomobile.loader.ImageLoader;
import cn.garymb.ygomobile.ui.activities.BaseActivity;
import cn.garymb.ygomobile.ui.activities.FileLogActivity;
import cn.garymb.ygomobile.ui.activities.WebActivity;
import cn.garymb.ygomobile.ui.adapters.ServerListAdapter;
import cn.garymb.ygomobile.ui.adapters.SimpleListAdapter;
import cn.garymb.ygomobile.ui.cards.CardDetailRandom;
import cn.garymb.ygomobile.ui.cards.CardSearchActivity;
import cn.garymb.ygomobile.ui.cards.DeckManagerActivity;
import cn.garymb.ygomobile.ui.cards.deck.DeckUtils;
import cn.garymb.ygomobile.ui.mycard.MyCardActivity;
import cn.garymb.ygomobile.ui.plus.DefaultOnBoomListener;
import cn.garymb.ygomobile.ui.plus.DialogPlus;
import cn.garymb.ygomobile.ui.plus.VUiKit;
import cn.garymb.ygomobile.ui.preference.SettingsActivity;
import cn.garymb.ygomobile.ui.widget.Shimmer;
import cn.garymb.ygomobile.ui.widget.ShimmerTextView;
import cn.garymb.ygomobile.utils.DownloadUtil;
import cn.garymb.ygomobile.utils.FileLogUtil;
import cn.garymb.ygomobile.utils.FileUtils;
import cn.garymb.ygomobile.utils.ScreenUtil;
import cn.garymb.ygomobile.utils.UnzipUtils;
import cn.garymb.ygomobile.utils.YGODialogUtil;
import cn.garymb.ygomobile.utils.YGOUtil;
import ocgcore.CardManager;
import ocgcore.DataManager;
import ocgcore.data.Card;

public abstract class HomeActivity extends BaseActivity implements OnDuelAssistantListener {

    private static final int ID_MAINACTIVITY = 0;

    protected SwipeMenuRecyclerView mServerList;
    long exitLasttime = 0;
    ShimmerTextView tv;
    Shimmer shimmer;
    private ServerListAdapter mServerListAdapter;
    private ServerListManager mServerListManager;
    private DuelAssistantManagement duelAssistantManagement;
    private CardManager mCardManager;
    private CardDetailRandom mCardDetailRandom;
    private ImageLoader mImageLoader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setExitAnimEnable(false);
        mImageLoader = new ImageLoader(false);
        mCardManager = DataManager.get().getCardManager();
        //server list
        initServerlist();
        //event
        EventBus.getDefault().register(this);
        initBoomMenuButton($(R.id.bmb));
        AnimationShake();
        tv = (ShimmerTextView) findViewById(R.id.shimmer_tv);
        toggleAnimation(tv);
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                if (arg0) {
                    Toast.makeText(getActivity(), "加载X5内核成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "加载系统内核成功", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCoreInitFinished() {
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(this, cb);
        if (!BuildConfig.BUILD_TYPE.equals("debug")) {
            //release才检查版本
            if (!Constants.ACTION_OPEN_GAME.equals(getIntent().getAction())) {
                Beta.checkUpgrade(false, false);
            }
        }
        //初始化决斗助手
        initDuelAssistant();
        //萌卡 FogMoe
        StartMycard();

        checkNotch();
        showNewbieGuide("homePage");
    }

    @Override
    protected void onResume() {
        super.onResume();
        BacktoDuel();
        duelAssistantCheck();
        //server list
        mServerListManager.syncLoadData();
    }

    @Override
    protected void onStop() {
        //mImageLoader.clearZipCache();
        super.onStop();
    }

    private void duelAssistantCheck() {
        if (AppsSettings.get().isServiceDuelAssistant()) {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                try {
                    FileLogUtil.writeAndTime("主页决斗助手检查");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                duelAssistantManagement.checkClip(ID_MAINACTIVITY);
            }, 500);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onJoinRoom(String host, int port, String password, int id) {
        if (id == ID_MAINACTIVITY) {
            quickjoinRoom(host, port, password);
        }
    }

    @Override
    public void onCardSearch(String key, int id) {
        if (id == ID_MAINACTIVITY) {
            Intent intent = new Intent(this, CardSearchActivity.class);
            intent.putExtra(CardSearchActivity.SEARCH_MESSAGE, key);
            startActivity(intent);
        }
    }

    @Override
    public void onSaveDeck(String message, boolean isUrl, int id) {
        if (id == ID_MAINACTIVITY) {
            saveDeck(message, isUrl);
        }
    }

    @Override
    public boolean isListenerEffective() {
        return Util.isContextExisted(this);
    }


    private void initDuelAssistant() {
        duelAssistantManagement = DuelAssistantManagement.getInstance();
        duelAssistantManagement.init(getApplicationContext());
        duelAssistantManagement.addDuelAssistantListener(this);
//        YGOUtil.startDuelService(this);
    }

    //检查是否有刘海
    private void checkNotch() {
        ScreenUtil.findNotchInformation(HomeActivity.this, new ScreenUtil.FindNotchInformation() {
            @Override
            public void onNotchInformation(boolean isNotch, int notchHeight, int phoneType) {
                AppsSettings.get().setNotchHeight(notchHeight);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        duelAssistantManagement.removeDuelAssistantListener(this);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServerInfoEvent(ServerInfoEvent event) {
        if (event.delete) {
            DialogPlus dialogPlus = new DialogPlus(getContext());
            dialogPlus.setTitle(R.string.question);
            dialogPlus.setMessage(R.string.delete_server_info);
            dialogPlus.setMessageGravity(Gravity.CENTER_HORIZONTAL);
            dialogPlus.setLeftButtonListener((dialog, which) -> {
                mServerListManager.delete(event.position);
                mServerListAdapter.notifyDataSetChanged();
                dialog.dismiss();
            });
            dialogPlus.setCancelable(true);
            dialogPlus.setOnCloseLinster(null);
            dialogPlus.show();
        } else if (event.join) {
            joinRoom(event.position);
            showNewbieGuide("joinRoom");
        } else {
            mServerListManager.showEditDialog(event.position);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (doMenu(item.getItemId())) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public HomeActivity getActivity() {
        return this;
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
    }

    private boolean doMenu(int id) {
        switch (id) {
            case R.id.nav_webpage: {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(BuildConfig.URL_DONATE));
                startActivity(intent);
            }
            break;
            case R.id.action_game:
                setRandomCardDetail();
                if (mCardDetailRandom != null) {
                    mCardDetailRandom.show();
                }
                openGame();
                break;
            case R.id.action_settings: {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.action_quit: {
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                DialogPlus builder = new DialogPlus(this);
                builder.setTitle(R.string.question);
                builder.setMessage(R.string.quit_tip);
                builder.setMessageGravity(Gravity.CENTER_HORIZONTAL);
                builder.setLeftButtonListener((dlg, s) -> {
                    dlg.dismiss();
                    finish();
                });
                builder.show();
            }
            break;
            case R.id.action_download_ex:
                WebActivity.open(this, getString(R.string.action_download_expansions), Constants.URL_YGO233_ADVANCE);
                break;
            case R.id.action_card_search:
                startActivity(new Intent(this, CardSearchActivity.class));
                break;
            case R.id.action_deck_manager:
                DeckManagerActivity.start(this, null);
                break;
            case R.id.action_join_qq_group:
                String key = "";
                joinQQGroup(key);
                break;
            case R.id.action_help: {
                final DialogPlus dialog = new DialogPlus(getContext());
                dialog.setContentView(R.layout.dialog_help);
                dialog.setTitle(R.string.question);
                dialog.show();
                View viewDialog = dialog.getContentView();
                Button btnMasterRule = viewDialog.findViewById(R.id.masterrule);
                Button btnTutorial = viewDialog.findViewById(R.id.tutorial);

                btnMasterRule.setOnClickListener((v) -> {
                    WebActivity.open(this, getString(R.string.masterrule), Constants.URL_MASTER_RULE_CN);
                    dialog.dismiss();
                });
                btnTutorial.setOnClickListener((v) -> {
                    WebActivity.open(this, getString(R.string.help), Constants.URL_HELP);
                    dialog.dismiss();
                });

            }
            break;
            case R.id.action_reset_game_res:
                updateImages();
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - exitLasttime <= 3000) {
            super.onBackPressed();
        } else {
            showToast(R.string.back_tip, Toast.LENGTH_SHORT);
            exitLasttime = System.currentTimeMillis();
        }
    }

    public void joinRoom(int position) {
        ServerInfo serverInfo = mServerListAdapter.getItem(position);
        if (serverInfo == null) {
            return;
        }
        //进入房间
        DialogPlus builder = new DialogPlus(getContext());
        builder.setTitle(R.string.intput_room_name);
        builder.setContentView(R.layout.dialog_room_name);
        EditText editText = builder.bind(R.id.room_name);
        ListView listView = builder.bind(R.id.room_list);
        TextView text_abt_roomlist = builder.bind(R.id.abt_room_list);
        SimpleListAdapter simpleListAdapter = new SimpleListAdapter(getContext());
        simpleListAdapter.set(AppsSettings.get().getLastRoomList());
        if (AppsSettings.get().getLastRoomList().size() > 0)
            text_abt_roomlist.setVisibility(View.VISIBLE);
        else text_abt_roomlist.setVisibility(View.GONE);
        listView.setAdapter(simpleListAdapter);
        listView.setOnItemClickListener((a, v, pos, index) -> {
            String name = simpleListAdapter.getItemById(index);
            editText.setText(name);
        });
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                builder.dismiss();
                String name = editText.getText().toString();
                if (!TextUtils.isEmpty(name)) {
                    List<String> items = simpleListAdapter.getItems();
                    int index = items.indexOf(name);
                    if (index >= 0) {
                        items.remove(index);
                        items.add(0, name);
                    } else {
                        items.add(0, name);
                    }
                    AppsSettings.get().setLastRoomList(items);
                    simpleListAdapter.notifyDataSetChanged();
                }
                joinGame(serverInfo, name);
                return true;
            }
            return false;
        });
        listView.setOnItemLongClickListener((a, v, i, index) -> {
            String name = simpleListAdapter.getItemById(index);
            int pos = simpleListAdapter.findItem(name);
            if (pos >= 0) {
                simpleListAdapter.remove(pos);
                simpleListAdapter.notifyDataSetChanged();
                AppsSettings.get().setLastRoomList(simpleListAdapter.getItems());
            }
            return true;
        });
        builder.setLeftButtonText(R.string.join_game);
        builder.setLeftButtonListener((dlg, i) -> {
            dlg.dismiss();
            if (Build.VERSION.SDK_INT >= 23 && YGOStarter.isGameRunning(getActivity())) {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.tip_return_to_duel, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                openGame();
            } else {
                //保存名字
                String name = editText.getText().toString();
                if (!TextUtils.isEmpty(name)) {
                    List<String> items = simpleListAdapter.getItems();
                    int index = items.indexOf(name);
                    if (index >= 0) {
                        items.remove(index);
                        items.add(0, name);
                    } else {
                        items.add(0, name);
                    }
                    AppsSettings.get().setLastRoomList(items);
                    simpleListAdapter.notifyDataSetChanged();
                }
                joinGame(serverInfo, name);
            }
        });
        builder.setOnCloseLinster((dlg) -> {
            dlg.dismiss();
        });
        builder.setOnCancelListener((dlg) -> {
        });
        builder.show();
    }

    void joinGame(ServerInfo serverInfo, String name) {
        showTipsToast();
        YGOGameOptions options = new YGOGameOptions();
        options.mServerAddr = serverInfo.getServerAddr();
        options.mUserName = serverInfo.getPlayerName();
        options.mPort = serverInfo.getPort();
        options.mRoomName = name;
        YGOStarter.startGame(this, options);
    }

    protected abstract void checkResourceDownload(ResCheckTask.ResCheckListener listener);

    protected abstract void openGame();

    public abstract void updateImages();

    private void initBoomMenuButton(BoomMenuButton menu) {
        final SparseArray<Integer> mMenuIds = new SparseArray<>();
        addMenuButton(mMenuIds, menu, R.id.action_join_qq_group, R.string.Join_QQ, R.drawable.joinqqgroup);
        addMenuButton(mMenuIds, menu, R.id.action_card_search, R.string.card_search, R.drawable.search);
        addMenuButton(mMenuIds, menu, R.id.action_deck_manager, R.string.deck_manager, R.drawable.deck);

        addMenuButton(mMenuIds, menu, R.id.action_download_ex, R.string.action_download_expansions, R.drawable.downloadimages);
        addMenuButton(mMenuIds, menu, R.id.action_game, R.string.action_game, R.drawable.start);
        addMenuButton(mMenuIds, menu, R.id.action_help, R.string.help, R.drawable.help);

        addMenuButton(mMenuIds, menu, R.id.action_reset_game_res, R.string.reset_game_res, R.drawable.reset);
        addMenuButton(mMenuIds, menu, R.id.action_settings, R.string.settings, R.drawable.setting);
        addMenuButton(mMenuIds, menu, R.id.nav_webpage, R.string.donation, R.drawable.about);

        //设置展开或隐藏的延时。 默认值为 800ms。
        menu.setDuration(100);
        //设置每两个子按钮之间动画的延时（ms为单位）。 比如，如果延时设为0，那么所有子按钮都会同时展开或隐藏，默认值为100ms。
        menu.setDelay(20);

        menu.setOnBoomListener(new DefaultOnBoomListener() {
            @Override
            public void onClicked(int index, BoomButton boomButton) {
                doMenu(mMenuIds.get(index));
            }
        });

    }

    private void addMenuButton(SparseArray<Integer> mMenuIds, BoomMenuButton menuButton, int menuId, int stringId, int image) {
        addMenuButton(mMenuIds, menuButton, menuId, getString(stringId), image);
    }

    private void addMenuButton(SparseArray<Integer> mMenuIds, BoomMenuButton menuButton, int menuId, String str, int image) {
        TextOutsideCircleButton.Builder builder = new TextOutsideCircleButton.Builder()
                .shadowColor(Color.TRANSPARENT)
                .normalColor(Color.TRANSPARENT)
                .normalImageRes(image)
                .normalText(str);
        menuButton.addBuilder(builder);
        mMenuIds.put(mMenuIds.size(), menuId);
    }

    public void AnimationShake() {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);//加载动画资源文件
        findViewById(R.id.cube).startAnimation(shake); //给组件播放动画效果
    }

    public void toggleAnimation(View target) {
        if (shimmer != null && shimmer.isAnimating()) {
            shimmer.cancel();
        } else {
            shimmer = new Shimmer();
            shimmer.start(tv);
        }
    }

    public void FogMoeUpdateCheck(){
        WebActivity webActivity = new WebActivity();
        File file = new File(AppsSettings.get().getResourcePath() + "FogMoe-Temp-YGO.zip");
        if (file.exists()) {
            FileUtils.deleteFile(file);
        }
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder
                .setTitle("下载")
                .setMessage("是否更新？（如果是第一次运行请先点击更新下载，否则没有卡片数据）")
                .setPositiveButton("更新", (dialog, which) -> {
                    String ygocorePath = AppsSettings.get().getResourcePath();
                    DownloadUtil.get().download("https://ygodiydata.github.fogmoe.top/",ygocorePath,"FogMoe-Temp-YGO.zip", new DownloadUtil.OnDownloadListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void onDownloadSuccess(File file1) {
                            Message message = new Message();
                            message.what = UnzipUtils.ZIP_READY;
                            try {
                                UnzipUtils.upZipFile(file1, AppsSettings.get().getResourcePath()+"/expansions");
                                FileUtils.copyDir(AppsSettings.get().getResourcePath()+"/expansions/FogMoeYGO-Card-Database-main",AppsSettings.get().getResourcePath()+"/expansions",true);
                                FileUtils.delFile(AppsSettings.get().getResourcePath()+"/expansions/FogMoeYGO-Card-Database-main");
                                FileUtils.delFile(AppsSettings.get().getResourcePath()+"/expansions/FogMoeYGO-Card-Database-main/script");
                                FileUtils.delFile(AppsSettings.get().getResourcePath()+"/expansions/FogMoeYGO-Card-Database-main/pics");
                                FileUtils.delFile(AppsSettings.get().getResourcePath()+"/expansions/FogMoeYGO-Card-Database-main/pics/field");
                            } catch (Exception e) {
                                message.what = UnzipUtils.ZIP_UNZIP_EXCEPTION;
                            } finally {
                                Looper.prepare();
                                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                NotificationChannel channel=new NotificationChannel("FogMoeYGO","卡片数据已完成更新下载，请重启App",
                                        NotificationManager.IMPORTANCE_HIGH);
                                manager.createNotificationChannel(channel);
                                Notification note = new NotificationCompat.Builder(getApplicationContext(), "FogMoeYGO")
                                        .setContentTitle("FogMoeYGO")
                                        .setContentText("卡片数据已完成更新下载，请重启App")
                                        .setSmallIcon(R.drawable.ic_launcher3)
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                        .build();
                                Toast.makeText(getApplicationContext(), "卡片数据已完成更新下载，请重启App", Toast.LENGTH_LONG).show();
                                note.flags=Notification.FLAG_INSISTENT|Notification.FLAG_AUTO_CANCEL;
                                manager.notify(1,note);
                                Looper.loop();
                                message.what = UnzipUtils.ZIP_UNZIP_OK;
                            }

                            //webActivity.handler.sendMessage(message);
                        }


                        @Override
                        public void onDownloading(int progress) {
                            Message message = new Message();
                            message.what = DownloadUtil.TYPE_DOWNLOAD_ING;
                            message.arg1 = progress;
                            //webActivity.handler.sendMessage(message);


                        }

                        @Override
                        public void onDownloadFailed(Exception e) {
                            //下载失败后删除下载的文件
                            FileUtils.deleteFile(file);
//                downloadCardImage(code, file);
                            Message message = new Message();
                            message.what = TYPE_DOWNLOAD_EXCEPTION;
                            message.obj = e.toString();
                            //webActivity.handler.sendMessage(message);
                        }
                    });
                    String[] str ={"正在下载卡片数据，请不要关闭程序，稍后会自动完成并弹出通知提示。"};
                    YGODialogUtil.dialogl(this,"正在下载卡片数据，请不要关闭程序，稍后会自动完成并弹出通知提示。",str);
                })
                .setNegativeButton("不更新", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alertDialog=builder.create();
        alertDialog.show();

    }

    public void StartMycard() {
        WebActivity webActivity = new WebActivity();
        ImageView iv_mc = $(R.id.btn_mycard);
        iv_mc.setOnClickListener((v) -> {
            FogMoeUpdateCheck();
        });
    }

    public void BacktoDuel() {
        tv.setOnClickListener((v) -> {
            openGame();
        });
        if (YGOStarter.isGameRunning(getActivity())) {
            tv.setVisibility(View.VISIBLE);
        } else {
            tv.setVisibility(View.GONE);
        }
    }

    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("https://jq.qq.com/?_wv=1027&k=RVS2wPU4"));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }

    private void saveDeck(String deckMessage, boolean isUrl) {
        DialogPlus dialog = new DialogPlus(this);
        dialog.setTitle(R.string.question);
        dialog.setMessage(R.string.find_deck_text);
        dialog.setMessageGravity(Gravity.CENTER_HORIZONTAL);
        dialog.setLeftButtonText(R.string.Cancel);
        dialog.setRightButtonText(R.string.save_n_open);
        dialog.show();
        dialog.setLeftButtonListener((dlg, s) -> {
            dialog.dismiss();
        });
        dialog.setRightButtonListener((dlg, s) -> {
            dialog.dismiss();
            //如果是卡组url
            if (isUrl) {
                Deck deckInfo = new Deck(getString(R.string.rename_deck) + System.currentTimeMillis(), Uri.parse(deckMessage));
                File file = deckInfo.saveTemp(AppsSettings.get().getDeckDir());
                if (!deckInfo.isCompleteDeck()) {
                    YGOUtil.show("当前卡组缺少完整信息，将只显示已有卡片");
                }
                DeckManagerActivity.start(this, file.getAbsolutePath());
            } else {
                //如果是卡组文本
                try {
                    //以当前时间戳作为卡组名保存卡组
                    File file = DeckUtils.save(getString(R.string.rename_deck) + System.currentTimeMillis(), deckMessage);
                    DeckManagerActivity.start(this, file.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, getString(R.string.save_failed_bcos) + e, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void quickjoinRoom(String host, int port, String password) {

        String message;
        if (!TextUtils.isEmpty(host))
            message = getString(R.string.quick_join)
                    + "\nIP：" + host
                    + "\n端口：" + port
                    + "\n密码：" + password;
        else
            message = getString(R.string.quick_join) + "：\"" + password + "\"";

        DialogPlus dialog = new DialogPlus(this);
        dialog.setTitle(R.string.question);
        dialog.setMessage(message);
        dialog.setMessageGravity(Gravity.CENTER_HORIZONTAL);
        dialog.setLeftButtonText(R.string.Cancel);
        dialog.setRightButtonText(R.string.join);
        dialog.show();
        dialog.setRightButtonListener((dlg, s) -> {
            dialog.dismiss();
            ServerListAdapter mServerListAdapter = new ServerListAdapter(this);
            ServerListManager mServerListManager = new ServerListManager(this, mServerListAdapter);
            mServerListManager.syncLoadData();
            File xmlFile = new File(getFilesDir(), Constants.SERVER_FILE);
            VUiKit.defer().when(() -> {
                ServerList assetList = ServerListManager.readList(this.getAssets().open(ASSET_SERVER_LIST));
                ServerList fileList = xmlFile.exists() ? ServerListManager.readList(new FileInputStream(xmlFile)) : null;
                if (fileList == null) {
                    return assetList;
                }
                if (fileList.getVercode() < assetList.getVercode()) {
                    xmlFile.delete();
                    return assetList;
                }
                return fileList;
            }).done((list) -> {
                if (list != null) {
                    String host1 = host;
                    int port1 = port;
                    ServerInfo serverInfo = list.getServerInfoList().get(0);
                    if (!TextUtils.isEmpty(host1)) {
                        serverInfo.setServerAddr(host1);
                        serverInfo.setPort(port1);
                    }
                    joinGame(serverInfo, password);
                }
            });
        });
        dialog.setLeftButtonListener((dlg, s) -> {
            dialog.dismiss();
        });
    }





    public void setRandomCardDetail() {
        //加载数据库中所有卡片卡片
        mCardManager.loadCards();
        //mCardManager = DataManager.get().getCardManager();
        SparseArray<Card> cards = mCardManager.getAllCards();
        int y = (int) (Math.random() * cards.size());
        Card cardInfo = cards.valueAt(y);
        if (cardInfo == null)
            return;
        mCardDetailRandom = CardDetailRandom.genRandomCardDetail(this, mImageLoader, cardInfo);
    }

    public void showTipsToast() {
        if (!YGOStarter.isGameRunning(getActivity())) {
            String[] tipsList = this.getResources().getStringArray(R.array.tips);
            int x = (int) (Math.random() * tipsList.length);
            String tips = tipsList[x];
            Toast.makeText(this, tips, Toast.LENGTH_LONG).show();
        }
    }

    public void initServerlist() {
        mServerList = $(R.id.list_server);
        mServerListAdapter = new ServerListAdapter(this);
        LayoutInflater infla = LayoutInflater.from(this);
        View footView = infla.inflate(R.layout.item_ic_add, null);
        TextView add_server = footView.findViewById(R.id.add_server);
        add_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mServerListManager.addServer();
            }
        });
        mServerListAdapter.addFooterView(footView);
        //server list
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mServerList.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mServerList.addItemDecoration(dividerItemDecoration);
        mServerList.setAdapter(mServerListAdapter);
        mServerListManager = new ServerListManager(this, mServerListAdapter);
        mServerListManager.bind(mServerList);
        mServerListManager.syncLoadData();
    }

    //https://www.jianshu.com/p/99649af3b191
    public void showNewbieGuide(String scene) {
        HighlightOptions options = new HighlightOptions.Builder()//绘制一个高亮虚线圈
                .setOnHighlightDrewListener(new OnHighlightDrewListener() {
                    @Override
                    public void onHighlightDrew(Canvas canvas, RectF rectF) {
                        Paint paint = new Paint();
                        paint.setColor(Color.WHITE);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(20);
                        paint.setPathEffect(new DashPathEffect(new float[]{20, 20}, 0));
                        canvas.drawCircle(rectF.centerX(), rectF.centerY(), rectF.width() / 2 + 10, paint);
                    }
                }).build();
        HighlightOptions options2 = new HighlightOptions.Builder()//绘制一个高亮虚线矩形
                .setOnHighlightDrewListener(new OnHighlightDrewListener() {
                    @Override
                    public void onHighlightDrew(Canvas canvas, RectF rectF) {
                        Paint paint = new Paint();
                        paint.setColor(Color.WHITE);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(20);
                        paint.setPathEffect(new DashPathEffect(new float[]{20, 20}, 0));
                        canvas.drawRect(rectF, paint);
                    }
                }).build();
        if (scene == "homePage") {
            NewbieGuide.with(this)//with方法可以传入Activity或者Fragment，获取引导页的依附者
                    .setLabel("homepageGuide")
                    .addGuidePage(
                            GuidePage.newInstance().setEverywhereCancelable(true)
                                    .setBackgroundColor(0xbc000000)
                                    .addHighLightWithOptions(findViewById(R.id.menu), HighLight.Shape.CIRCLE, options)
                                    .setLayoutRes(R.layout.view_guide_home)
                                    .setOnLayoutInflatedListener(new OnLayoutInflatedListener() {

                                        @Override
                                        public void onLayoutInflated(View view, Controller controller) {
                                            //可只创建一个引导layout并把相关内容都放在其中并GONE，获得ID并初始化相应为显示
                                            view.findViewById(R.id.view_abt_menu).setVisibility(View.VISIBLE);
                                        }
                                    })

                    )
                    .addGuidePage(
                            GuidePage.newInstance().setEverywhereCancelable(true)
                                    .setBackgroundColor(0xbc000000)
                                    .addHighLightWithOptions(findViewById(R.id.mycard), HighLight.Shape.CIRCLE, options)
                                    .setLayoutRes(R.layout.view_guide_home)
                                    .setOnLayoutInflatedListener(new OnLayoutInflatedListener() {

                                        @Override
                                        public void onLayoutInflated(View view, Controller controller) {
                                            TextView tv = view.findViewById(R.id.text_about);
                                            tv.setVisibility(View.VISIBLE);
                                            tv.setText(R.string.guide_mycard);
                                        }
                                    })
                    )
                    .addGuidePage(
                            GuidePage.newInstance().setEverywhereCancelable(true)
                                    .setBackgroundColor(0xbc000000)
                                    .addHighLightWithOptions(findViewById(R.id.list_server), HighLight.Shape.ROUND_RECTANGLE, options2)
                                    .setLayoutRes(R.layout.view_guide_home)
                                    .setOnLayoutInflatedListener(new OnLayoutInflatedListener() {

                                        @Override
                                        public void onLayoutInflated(View view, Controller controller) {
                                            TextView tv = view.findViewById(R.id.text_about);
                                            tv.setVisibility(View.VISIBLE);
                                            tv.setText(R.string.guide_serverlist);
                                        }
                                    })
                    )
                    .addGuidePage(
                            GuidePage.newInstance().setEverywhereCancelable(true)
                                    .setBackgroundColor(0xbc000000)
                                    .setLayoutRes(R.layout.view_guide_home)
                                    .setOnLayoutInflatedListener(new OnLayoutInflatedListener() {

                                        @Override
                                        public void onLayoutInflated(View view, Controller controller) {
                                            view.findViewById(R.id.view_abt_server_edit).setVisibility(View.VISIBLE);
                                        }
                                    })
                    )
                    //.alwaysShow(true)//总是显示，调试时可以打开
                    .show();
        } else if (scene == "joinRoom") {
            NewbieGuide.with(this)
                    .setLabel("joinRoomGuide")
                    .addGuidePage(
                            GuidePage.newInstance().setEverywhereCancelable(true)
                                    .setBackgroundColor(0xbc000000)
                                    .setLayoutRes(R.layout.view_guide_home)
                                    .setOnLayoutInflatedListener(new OnLayoutInflatedListener() {

                                        @Override
                                        public void onLayoutInflated(View view, Controller controller) {
                                            view.findViewById(R.id.view_abt_join_room).setVisibility(View.VISIBLE);
                                        }
                                    })

                    )
                    .show();
        }
    }
}
