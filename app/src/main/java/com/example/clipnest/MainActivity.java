package com.example.clipnest;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.Manifest;
import android.content.ContentValues;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.view.ViewOutlineProvider;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.system.ErrnoException;
import android.system.Os;
import android.text.InputType;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import com.yausername.ffmpeg.FFmpeg;
import com.yausername.youtubedl_android.YoutubeDL;
import com.yausername.youtubedl_android.YoutubeDLException;
import com.yausername.youtubedl_android.YoutubeDLRequest;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import kotlin.Unit;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(13, 17, 23);
    private static final int PANEL = Color.rgb(22, 27, 34);
    private static final int ACCENT = Color.rgb(61, 214, 198);
    private static final int TEXT = Color.rgb(235, 241, 247);
    private static final int MUTED = Color.rgb(154, 166, 178);
    private static final int DANGER = Color.rgb(255, 132, 145);
    private static final int FOLDER_PICKER_REQUEST = 42;
    private static final int GALLERY_FOLDER_PICKER_REQUEST = 45;
    private static final int COOKIE_IMPORT_REQUEST = 43;
    private static final int COOKIE_EXPORT_REQUEST = 44;
    private static final int NOTIFICATION_PERMISSION_REQUEST = 501;
    private static final int DOWNLOAD_NOTIFICATION_ID = 7001;
    private static final String DOWNLOAD_CHANNEL_ID = "clipnest_downloads";
    private static final String PREFS = "clipnest_settings";
    private static final String PREF_FOLDER_URI = "destination_tree_uri";
    private static final String PREF_GALLERY_FOLDER_URI = "gallery_destination_tree_uri";
    private static final String PREF_VIDEO_QUALITY = "video_quality";
    private static final String PREF_AUDIO_QUALITY = "audio_quality";
    private static final String PREF_WEB_JS = "web_javascript";
    private static final String PREF_WEB_COOKIES = "web_cookies";
    private static final String PREF_WEB_THIRD_PARTY_COOKIES = "web_third_party_cookies";
    private static final String PREF_WEB_DOM_STORAGE = "web_dom_storage";
    private static final String PREF_WEB_ZOOM = "web_zoom_controls";
    private static final String PREF_IMPORTED_COOKIE_FILE = "imported_cookie_file";
    private static final String PREF_COOKIE_USER_AGENT = "cookie_user_agent";
    private static final String PREF_LANGUAGE = "language";
    private static final String PREF_BOOKMARKS = "browser_bookmarks";
    private static final String PREF_ANIMATIONS = "ui_animations";
    private static final String PREF_KEEP_SCREEN_ON = "keep_screen_on";

    private static final String[] VIDEO_QUALITY_LABELS = {
            "Best available", "Up to 2160p", "Up to 1440p", "Up to 1080p", "Up to 720p", "Up to 480p", "Up to 360p"
    };
    private static final String[] AUDIO_QUALITY_LABELS = {
            "Best MP3 · V0", "High MP3 · V2", "Standard MP3 · V5", "MP3 · 128 kbps"
    };
    private static final String[] AUDIO_QUALITY_ARGS = {"0", "2", "5", "128K"};
    private static final int[] VIDEO_HEIGHT_LIMITS = {0, 2160, 1440, 1080, 720, 480, 360};

    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private EditText urlInput;
    private EditText galleryUrlInput;
    private RadioButton videoChoice;
    private TextView videoQualityChoice;
    private TextView audioQualityChoice;
    private LinearLayout videoQualityRow;
    private LinearLayout audioQualityRow;
    private Button downloadButton;
    private Button galleryDownloadButton;
    private Button galleryFolderButton;
    private Button galleryCopyErrorButton;
    private TextView galleryStatusText;
    private TextView galleryDestinationText;
    private RadioGroup galleryOutputMode;
    private Button updateYtDlpButton;
    private Button updatePageButton;
    private Button cancelButton;
    private Button folderButton;
    private Button openButton;
    private ProgressBar progressBar;
    private TextView statusText;
    private Button copyErrorButton;
    private TextView destinationText;
    private TextView installedVersionText;
    private TextView updatePageStatusText;
    private TextView galleryVersionText;
    private TextView galleryUpdateStatusText;
    private Button galleryUpdateButton;
    private TextView browserStatusText;
    private TextView browserNoteText;
    private TextView cookieStatusText;
    private TextView webConfigStatusText;
    private TextView batteryStatusText;
    private Button batteryButton;
    private Uri selectedGalleryFolderUri;
    private TextView[] navigationTabs;
    private View[] navigationPages;
    private EditText browserAddressInput;
    private WebView browserWebView;
    private ProgressBar browserProgress;
    private boolean browserPageLoaded;
    private int currentPage = -1;
    private Button bookmarkListButton;
    private final List<String> bookmarks = new ArrayList<>();
    private String processId;
    private volatile boolean cancelRequested;
    private Uri lastSavedUri;
    private String lastMimeType = "video/mp4";
    private Uri selectedFolderUri;
    private File importedCookieFile;
    private String importedCookieUserAgent;
    private String pendingCookieExportText;
    private NotificationManager notificationManager;
    private volatile boolean downloadInProgress;
    private boolean engineReady;
    private int videoQualityIndex;
    private int audioQualityIndex;
    private volatile boolean updateInProgress;
    private volatile String updateStatusSummary = "Not checked yet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createDownloadNotificationChannel();
        restoreBookmarks();
        buildInterface();
        restorePreferences();
        restoreImportedCookieFile();
        requestNotificationPermissionIfNeeded();
        setStatus("Starting the yt-dlp engine…");
        worker.execute(() -> {
            try {
                YoutubeDL.getInstance().init(getApplicationContext());
                FFmpeg.getInstance().init(getApplicationContext());
                engineReady = true;
                updateInProgress = true;
                main.post(() -> {
                    setStatus("Ready. Checking yt-dlp version…");
                    downloadButton.setEnabled(true);
                    setUpdateButtonsEnabled(true);
                });
                try {
                    updateStatusSummary = updateRecommendedYtDlp();
                } catch (Exception updateError) {
                    updateStatusSummary = "Automatic update check failed: " + safeMessage(updateError);
                } finally {
                    updateInProgress = false;
                    main.post(() -> {
                        setUpdateButtonsEnabled(true);
                        refreshInstalledVersion();
                        if (updatePageStatusText != null) updatePageStatusText.setText(updateStatusSummary);
                        if (statusText.getText().toString().startsWith("Ready. Checking")) {
                            setStatus("Ready. " + updateStatusSummary);
                        }
                    });
                }
            } catch (YoutubeDLException e) {
                main.post(() -> setStatus("Could not start yt-dlp: " + safeMessage(e)));
            }
        });
    }

    private void createDownloadNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= 26 && notificationManager != null) {
            NotificationChannel channel = new NotificationChannel(
                    DOWNLOAD_CHANNEL_ID, "ClipNest downloads", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Download progress and cancellation controls");
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (android.os.Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (DownloadForegroundService.ACTION_CANCEL_DOWNLOAD.equals(intent.getAction()) && downloadInProgress) cancelDownload();
    }

    private boolean isArabic() {
        return "ar".equals(getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_LANGUAGE, "en"));
    }

    private String tr(String english, String arabic) {
        return isArabic() ? arabic : english;
    }

    private void buildInterface() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setLayoutDirection(isArabic() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        LinearLayout tabBar = new LinearLayout(this);
        tabBar.setOrientation(LinearLayout.HORIZONTAL);
        tabBar.setGravity(Gravity.CENTER_VERTICAL);
        tabBar.setPadding(dp(5), dp(5), dp(5), dp(5));
        tabBar.setBackground(rounded(PANEL, 18));
        tabBar.setClipToOutline(true);
        LinearLayout.LayoutParams tabBarParams = params(-1, dp(58));
        tabBarParams.setMargins(dp(12), dp(6), dp(12), dp(8));
        String[] tabNames = {tr("Download", "التنزيل"), tr("Updates", "التحديثات"), tr("Browser", "المتصفح"), tr("Web config", "إعدادات الويب")};
        navigationTabs = new TextView[tabNames.length];
        for (int i = 0; i < tabNames.length; i++) {
            final int pageIndex = i;
            TextView tab = new TextView(this);
            tab.setText(tabNames[i]);
            tab.setTextSize(i == 3 ? 11 : 13);
            tab.setGravity(Gravity.CENTER);
            tab.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            tab.setPadding(dp(6), 0, dp(6), 0);
            tab.setBackground(rounded(Color.TRANSPARENT, 13));
            tab.setClipToOutline(true);
            LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(0, -1, 1f);
            if (i > 0) tabParams.leftMargin = dp(4);
            tabBar.addView(tab, tabParams);
            navigationTabs[i] = tab;
            tab.setOnClickListener(v -> showPage(pageIndex));
        }

        FrameLayout pageHost = new FrameLayout(this);
        root.addView(pageHost, new LinearLayout.LayoutParams(-1, 0, 1f));

        ScrollView scroller = new ScrollView(this);
        scroller.setFillViewport(true);
        scroller.setBackgroundColor(BG);
        pageHost.addView(scroller, new FrameLayout.LayoutParams(-1, -1));
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(24), dp(24), dp(24), dp(28));
        scroller.addView(page);

        TextView brand = new TextView(this);
        brand.setText("CLIPNEST");
        brand.setTextColor(ACCENT);
        brand.setTextSize(13);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        brand.setLetterSpacing(0.14f);
        page.addView(brand);

        TextView title = new TextView(this);
        title.setText(tr("Save a link.\nKeep it simple.", "احفظ الرابط.\nبكل بساطة."));
        title.setTextColor(TEXT);
        title.setTextSize(31);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setLineSpacing(dp(2), 1.02f);
        LinearLayout.LayoutParams titleParams = params(-1, -2);
        titleParams.topMargin = dp(15);
        page.addView(title, titleParams);

        TextView subtitle = new TextView(this);
        subtitle.setText(tr("Downloads run on your device with yt-dlp and Gallery-dl.", "تعمل التنزيلات على جهازك باستخدام yt-dlp وGallery-dl."));
        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(15);
        LinearLayout.LayoutParams subParams = params(-1, -2);
        subParams.topMargin = dp(9);
        page.addView(subtitle, subParams);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(rounded(PANEL, 20));
        LinearLayout.LayoutParams cardParams = params(-1, -2);
        cardParams.topMargin = dp(25);
        page.addView(card, cardParams);

        card.addView(label(tr("VIDEO OR AUDIO LINK", "رابط الفيديو أو الصوت")));
        Button ytGuideButton = actionButton(tr("How to use yt-dlp", "طريقة استخدام yt-dlp"), Color.rgb(26, 39, 48), ACCENT, Color.rgb(50, 73, 82), 12);
        LinearLayout.LayoutParams ytGuideParams = params(-1, dp(40));
        ytGuideParams.topMargin = dp(8);
        card.addView(ytGuideButton, ytGuideParams);
        ytGuideButton.setOnClickListener(v -> showSimpleInfoDialog(
                tr("How to use yt-dlp", "طريقة استخدام yt-dlp"),
                tr("1. Paste a video link.\n2. Choose a video or MP3 quality.\n3. Choose a save folder, then tap Download.\n\nYouTube sign-in cookies are optional and should only be exported from your own WebView session.",
                        "١. الصق رابط الفيديو.\n٢. اختر جودة الفيديو أو MP3.\n٣. اختر مجلد الحفظ ثم اضغط تنزيل.\n\nملفات تسجيل الدخول إلى YouTube اختيارية، ويجب تصديرها فقط من جلسة WebView الخاصة بك.")));
        urlInput = new EditText(this);
        urlInput.setSingleLine(true);
        urlInput.setHint(tr("Video link", "رابط الفيديو"));
        urlInput.setHintTextColor(MUTED);
        urlInput.setTextColor(TEXT);
        urlInput.setTextSize(16);
        urlInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        urlInput.setPadding(dp(14), 0, dp(56), 0);
        urlInput.setBackground(rounded(Color.rgb(12, 15, 20), 12));
        LinearLayout.LayoutParams inputParams = params(-1, dp(54));
        inputParams.topMargin = dp(10);
        FrameLayout urlFrame = new FrameLayout(this);
        card.addView(urlFrame, inputParams);
        urlFrame.addView(urlInput, new FrameLayout.LayoutParams(-1, -1));
        Button clearUrlButton = actionButton("×", Color.rgb(38, 44, 52), TEXT, Color.rgb(58, 66, 76), 18);
        clearUrlButton.setContentDescription(tr("Clear URL", "مسح الرابط"));
        clearUrlButton.setPadding(0, 0, 0, 0);
        FrameLayout.LayoutParams clearUrlParams = new FrameLayout.LayoutParams(dp(42), dp(42), Gravity.END | Gravity.CENTER_VERTICAL);
        clearUrlParams.rightMargin = dp(6);
        urlFrame.addView(clearUrlButton, clearUrlParams);
        clearUrlButton.setVisibility(View.GONE);
        clearUrlButton.setOnClickListener(v -> urlInput.setText(""));
        urlInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearUrlButton.setVisibility(s != null && s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        LinearLayout folderRow = new LinearLayout(this);
        folderRow.setGravity(android.view.Gravity.CENTER_VERTICAL);
        folderRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams folderRowParams = params(-1, -2);
        folderRowParams.topMargin = dp(17);
        card.addView(folderRow, folderRowParams);
        folderButton = actionButton(tr("Choose folder", "اختيار المجلد"), Color.rgb(26, 39, 48), ACCENT, ACCENT, 13);
        folderRow.addView(folderButton, params(-2, dp(44)));
        destinationText = new TextView(this);
        destinationText.setTextColor(MUTED);
        destinationText.setTextSize(12);
        destinationText.setMaxLines(2);
        destinationText.setPadding(dp(12), 0, 0, 0);
        folderRow.addView(destinationText, new LinearLayout.LayoutParams(0, -2, 1f));

        LinearLayout.LayoutParams formatLabelParams = params(-1, -2);
        formatLabelParams.topMargin = dp(20);
        card.addView(label(tr("DOWNLOAD AS", "تنزيل كـ")), formatLabelParams);

        RadioGroup formats = new RadioGroup(this);
        formats.setOrientation(RadioGroup.HORIZONTAL);
        videoChoice = new RadioButton(this);
        videoChoice.setId(View.generateViewId());
        videoChoice.setText(tr("Video", "فيديو"));
        videoChoice.setTextColor(TEXT);
        videoChoice.setTextSize(14);
        videoChoice.setButtonTintList(ColorStateList.valueOf(ACCENT));
        RadioButton audioChoice = new RadioButton(this);
        audioChoice.setId(View.generateViewId());
        audioChoice.setText(tr("Audio · MP3", "صوت · MP3"));
        audioChoice.setTextColor(TEXT);
        audioChoice.setTextSize(14);
        audioChoice.setButtonTintList(ColorStateList.valueOf(ACCENT));
        LinearLayout.LayoutParams choiceParams = params(-2, dp(48));
        choiceParams.rightMargin = dp(18);
        formats.addView(videoChoice, choiceParams);
        formats.addView(audioChoice, params(-2, dp(48)));
        formats.check(videoChoice.getId());
        card.addView(formats);

        videoQualityRow = qualityRow(card, tr("VIDEO QUALITY", "جودة الفيديو"), true);
        videoQualityChoice = (TextView) videoQualityRow.getTag();
        audioQualityRow = qualityRow(card, tr("AUDIO QUALITY", "جودة الصوت"), false);
        audioQualityChoice = (TextView) audioQualityRow.getTag();
        updateQualityLabels();
        audioQualityRow.setVisibility(View.GONE);
        formats.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isVideo = checkedId == videoChoice.getId();
            videoQualityRow.setVisibility(isVideo ? View.VISIBLE : View.GONE);
            audioQualityRow.setVisibility(isVideo ? View.GONE : View.VISIBLE);
        });

        downloadButton = actionButton(tr("Download to device", "تنزيل إلى الجهاز"), ACCENT, Color.rgb(4, 26, 28), ACCENT, 15);
        downloadButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        downloadButton.setEnabled(false);
        LinearLayout.LayoutParams downloadParams = params(-1, dp(54));
        downloadParams.topMargin = dp(19);
        card.addView(downloadButton, downloadParams);

        cancelButton = actionButton(tr("×   Cancel download", "×   إلغاء التنزيل"), Color.rgb(54, 27, 34), DANGER, Color.rgb(119, 56, 66), 14);
        cancelButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        cancelButton.setVisibility(View.GONE);
        LinearLayout.LayoutParams cancelParams = params(-1, dp(48));
        cancelParams.topMargin = dp(9);
        card.addView(cancelButton, cancelParams);

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgressTintList(ColorStateList.valueOf(ACCENT));
        progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(48, 56, 66)));
        progressBar.setVisibility(View.GONE);
        LinearLayout.LayoutParams progressParams = params(-1, dp(5));
        progressParams.topMargin = dp(18);
        card.addView(progressBar, progressParams);

        statusText = new TextView(this);
        statusText.setTextColor(MUTED);
        statusText.setTextSize(14);
        statusText.setTextIsSelectable(true);
        statusText.setLongClickable(true);
        statusText.setLineSpacing(dp(3), 1.0f);
        LinearLayout.LayoutParams statusParams = params(-1, -2);
        statusParams.topMargin = dp(14);
        card.addView(statusText, statusParams);

        openButton = actionButton(tr("Open downloaded file", "فتح الملف المنزّل"), Color.rgb(26, 39, 48), ACCENT, Color.rgb(50, 73, 82), 14);
        openButton.setVisibility(View.GONE);
        LinearLayout.LayoutParams openParams = params(-1, dp(46));
        openParams.topMargin = dp(8);
        card.addView(openButton, openParams);

        LinearLayout galleryCard = new LinearLayout(this);
        galleryCard.setOrientation(LinearLayout.VERTICAL);
        galleryCard.setPadding(dp(18), dp(18), dp(18), dp(18));
        galleryCard.setBackground(rounded(PANEL, 20));
        LinearLayout.LayoutParams galleryCardParams = params(-1, -2);
        galleryCardParams.topMargin = dp(16);
        page.addView(galleryCard, galleryCardParams);
        galleryCard.addView(label(tr("GALLERY-DL LINK", "رابط Gallery-dl")));
        Button galleryGuideButton = actionButton(tr("How to use Gallery-dl", "طريقة استخدام Gallery-dl"), Color.rgb(26, 39, 48), ACCENT, Color.rgb(50, 73, 82), 12);
        LinearLayout.LayoutParams galleryGuideParams = params(-1, dp(40));
        galleryGuideParams.topMargin = dp(8);
        galleryCard.addView(galleryGuideButton, galleryGuideParams);
        galleryGuideButton.setOnClickListener(v -> showSimpleInfoDialog(
                tr("How to use Gallery-dl", "طريقة استخدام Gallery-dl"),
                tr("1. Paste a supported gallery, profile, or image URL.\n2. Choose the Gallery-dl folder, or keep the yt-dlp folder as the default.\n3. Tap Download with Gallery-dl.\n\nA NoExtractorError means Gallery-dl does not support that URL or the URL is only a wrapper/search page.",
                        "١. الصق رابط معرض أو ملف شخصي أو صورة مدعومًا.\n٢. اختر مجلد Gallery-dl أو اترك مجلد yt-dlp كافتراضي.\n٣. اضغط التنزيل باستخدام Gallery-dl.\n\nيعني NoExtractorError أن Gallery-dl لا يدعم الرابط أو أن الرابط مجرد صفحة وسيطة أو بحث.")));
        galleryUrlInput = new EditText(this);
        galleryUrlInput.setSingleLine(true);
        galleryUrlInput.setHint(tr("Gallery or image link", "رابط معرض أو صورة"));
        galleryUrlInput.setHintTextColor(MUTED);
        galleryUrlInput.setTextColor(TEXT);
        galleryUrlInput.setTextSize(15);
        galleryUrlInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        galleryUrlInput.setPadding(dp(14), 0, dp(56), 0);
        GradientDrawable galleryInputBackground = rounded(Color.rgb(12, 15, 20), 12);
        galleryInputBackground.setStroke(dp(1), Color.rgb(65, 86, 96));
        galleryUrlInput.setBackground(galleryInputBackground);
        LinearLayout.LayoutParams galleryInputParams = params(-1, dp(52));
        galleryInputParams.topMargin = dp(10);
        FrameLayout galleryUrlFrame = new FrameLayout(this);
        galleryCard.addView(galleryUrlFrame, galleryInputParams);
        galleryUrlFrame.addView(galleryUrlInput, new FrameLayout.LayoutParams(-1, -1));
        Button clearGalleryButton = actionButton("×", Color.rgb(38, 44, 52), TEXT, Color.rgb(58, 66, 76), 18);
        clearGalleryButton.setContentDescription(tr("Clear Gallery-dl link", "مسح رابط Gallery-dl"));
        clearGalleryButton.setPadding(0, 0, 0, 0);
        FrameLayout.LayoutParams clearGalleryParams = new FrameLayout.LayoutParams(dp(42), dp(42), Gravity.END | Gravity.CENTER_VERTICAL);
        clearGalleryParams.rightMargin = dp(6);
        galleryUrlFrame.addView(clearGalleryButton, clearGalleryParams);
        clearGalleryButton.setVisibility(View.GONE);
        clearGalleryButton.setOnClickListener(v -> galleryUrlInput.setText(""));
        galleryUrlInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { clearGalleryButton.setVisibility(s != null && s.length() > 0 ? View.VISIBLE : View.GONE); }
            @Override public void afterTextChanged(Editable s) { }
        });
        LinearLayout galleryFolderRow = new LinearLayout(this);
        galleryFolderRow.setGravity(Gravity.CENTER_VERTICAL);
        galleryFolderRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams galleryFolderRowParams = params(-1, -2);
        galleryFolderRowParams.topMargin = dp(10);
        galleryCard.addView(galleryFolderRow, galleryFolderRowParams);
        galleryFolderButton = actionButton(tr("Choose Gallery-dl folder", "اختيار مجلد Gallery-dl"), Color.rgb(26, 39, 48), ACCENT, ACCENT, 12);
        galleryFolderRow.addView(galleryFolderButton, params(-2, dp(44)));
        galleryDestinationText = new TextView(this);
        galleryDestinationText.setTextColor(MUTED);
        galleryDestinationText.setTextSize(12);
        galleryDestinationText.setMaxLines(2);
        galleryDestinationText.setPadding(dp(12), 0, 0, 0);
        galleryFolderRow.addView(galleryDestinationText, new LinearLayout.LayoutParams(0, -2, 1f));
        galleryCard.addView(label(tr("OUTPUT FORMAT", "صيغة الإخراج")));
        galleryOutputMode = new RadioGroup(this);
        galleryOutputMode.setOrientation(RadioGroup.HORIZONTAL);
        RadioButton folderOutput = galleryOutputButton(tr("Folder", "مجلد"), "folder");
        RadioButton zipOutput = galleryOutputButton("ZIP", "zip");
        RadioButton cbzOutput = galleryOutputButton("CBZ", "cbz");
        galleryOutputMode.addView(folderOutput);
        galleryOutputMode.addView(zipOutput);
        galleryOutputMode.addView(cbzOutput);
        galleryOutputMode.check(folderOutput.getId());
        LinearLayout.LayoutParams galleryOutputParams = params(-1, dp(48));
        galleryOutputParams.topMargin = dp(4);
        galleryCard.addView(galleryOutputMode, galleryOutputParams);
        galleryDownloadButton = actionButton(tr("Download with Gallery-dl", "التنزيل باستخدام Gallery-dl"), Color.rgb(26, 54, 56), ACCENT, Color.rgb(45, 104, 103), 13);
        galleryDownloadButton.setEnabled(false);
        LinearLayout.LayoutParams galleryButtonParams = params(-1, dp(48));
        galleryButtonParams.topMargin = dp(10);
        galleryCard.addView(galleryDownloadButton, galleryButtonParams);
        galleryStatusText = new TextView(this);
        galleryStatusText.setTextColor(MUTED);
        galleryStatusText.setTextSize(12);
        galleryStatusText.setTextIsSelectable(true);
        galleryStatusText.setLongClickable(true);
        galleryStatusText.setFocusable(true);
        galleryStatusText.setText(tr("Enter a link, then choose Download with Gallery-dl.", "أدخل رابطًا ثم اختر التنزيل باستخدام Gallery-dl."));
        galleryStatusText.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams galleryStatusParams = params(-1, -2);
        galleryStatusParams.topMargin = dp(8);
        galleryCard.addView(galleryStatusText, galleryStatusParams);
        galleryUrlInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { galleryDownloadButton.setEnabled(s != null && s.toString().trim().length() > 0); }
            @Override public void afterTextChanged(Editable s) { }
        });

        TextView footer = new TextView(this);
        footer.setText(tr("Only save media you own or are authorized to download. Respect source-site terms and local law. ClipNest does not export login cookies or bypass age restrictions, DRM, or other access controls.", "احفظ فقط الوسائط التي تملكها أو تملك تصريحًا لتنزيلها. احترم شروط المواقع والقوانين المحلية. لا يصدّر ClipNest ملفات تسجيل الدخول ولا يتجاوز قيود العمر أو DRM أو ضوابط الوصول الأخرى."));
        footer.setTextColor(MUTED);
        footer.setTextSize(12);
        footer.setLineSpacing(dp(4), 1.0f);
        LinearLayout.LayoutParams footerParams = params(-1, -2);
        footerParams.topMargin = dp(8);
        page.addView(footer, footerParams);

        ScrollView updatesPage = buildUpdatePage();
        View browserPage = buildBrowserPage();
        ScrollView webConfigPage = buildWebConfigPage();
        pageHost.addView(updatesPage, new FrameLayout.LayoutParams(-1, -1));
        pageHost.addView(browserPage, new FrameLayout.LayoutParams(-1, -1));
        pageHost.addView(webConfigPage, new FrameLayout.LayoutParams(-1, -1));
        root.addView(tabBar, tabBarParams);
        navigationPages = new View[]{scroller, updatesPage, browserPage, webConfigPage};
        setContentView(root);
        showPage(0);
        downloadButton.setOnClickListener(v -> startDownload());
        cancelButton.setOnClickListener(v -> cancelDownload());
        folderButton.setOnClickListener(v -> chooseFolder());
        openButton.setOnClickListener(v -> openSavedFile());
        galleryDownloadButton.setOnClickListener(v -> startGalleryDownload());
        galleryFolderButton.setOnClickListener(v -> chooseGalleryFolder());
    }

    private void showPage(int selectedPage) {
        if (navigationPages == null || navigationTabs == null) return;
        boolean shouldAnimate = currentPage >= 0 && currentPage != selectedPage
                && getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(PREF_ANIMATIONS, true);
        int direction = currentPage < selectedPage ? 1 : -1;
        for (int i = 0; i < navigationPages.length; i++) {
            boolean selected = i == selectedPage;
            navigationPages[i].setVisibility(selected ? View.VISIBLE : View.GONE);
            if (selected) {
                navigationPages[i].setTranslationX(shouldAnimate ? direction * dp(28) : 0);
                navigationPages[i].setAlpha(shouldAnimate ? 0.72f : 1f);
                if (shouldAnimate) {
                    navigationPages[i].animate().translationX(0).alpha(1f).setDuration(180).start();
                }
            } else {
                navigationPages[i].animate().cancel();
                navigationPages[i].setTranslationX(0);
                navigationPages[i].setAlpha(1f);
            }
            GradientDrawable background = rounded(selected ? Color.rgb(30, 49, 58) : Color.TRANSPARENT, 13);
            if (selected) background.setStroke(dp(1), Color.rgb(48, 101, 103));
                navigationTabs[i].setBackground(background);
            navigationTabs[i].setTextColor(selected ? ACCENT : MUTED);
        }
        currentPage = selectedPage;
        if (selectedPage == 2 && !browserPageLoaded && browserWebView != null) {
            browserPageLoaded = true;
            browserWebView.loadUrl("https://www.youtube.com");
        }
    }

    private ScrollView buildUpdatePage() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(24), dp(28), dp(24), dp(30));
        scroll.addView(page);

        page.addView(label(tr("MAINTENANCE", "الصيانة")));
        TextView title = new TextView(this);
        title.setText(tr("yt-dlp updates", "تحديثات yt-dlp"));
        title.setTextColor(TEXT);
        title.setTextSize(30);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = params(-1, -2);
        titleParams.topMargin = dp(10);
        page.addView(title, titleParams);

        TextView intro = new TextView(this);
        intro.setText(tr("Check for or install the latest nightly build. This page updates the yt-dlp engine used by downloads.", "تحقق من أحدث إصدار ليلي أو ثبّته. تحدّث هذه الصفحة محرّك yt-dlp المستخدم للتنزيلات."));
        intro.setTextColor(MUTED);
        intro.setTextSize(15);
        intro.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams introParams = params(-1, -2);
        introParams.topMargin = dp(8);
        page.addView(intro, introParams);

        LinearLayout infoCard = new LinearLayout(this);
        infoCard.setOrientation(LinearLayout.VERTICAL);
        infoCard.setPadding(dp(18), dp(18), dp(18), dp(18));
        infoCard.setBackground(rounded(PANEL, 20));
        LinearLayout.LayoutParams infoCardParams = params(-1, -2);
        infoCardParams.topMargin = dp(22);
        page.addView(infoCard, infoCardParams);

        TextView channelLabel = label(tr("UPDATE CHANNEL", "قناة التحديث"));
        infoCard.addView(channelLabel);
        TextView channelValue = new TextView(this);
        channelValue.setText(tr("Nightly · recommended by yt-dlp for regular use", "ليلي · يوصي به yt-dlp للاستخدام العادي"));
        channelValue.setTextColor(TEXT);
        channelValue.setTextSize(15);
        LinearLayout.LayoutParams channelParams = params(-1, -2);
        channelParams.topMargin = dp(7);
        infoCard.addView(channelValue, channelParams);

        TextView versionLabel = label(tr("INSTALLED VERSION", "الإصدار المثبّت"));
        LinearLayout.LayoutParams versionLabelParams = params(-1, -2);
        versionLabelParams.topMargin = dp(18);
        infoCard.addView(versionLabel, versionLabelParams);
        installedVersionText = new TextView(this);
        installedVersionText.setTextColor(ACCENT);
        installedVersionText.setTextSize(14);
        LinearLayout.LayoutParams versionParams = params(-1, -2);
        versionParams.topMargin = dp(7);
        infoCard.addView(installedVersionText, versionParams);
        refreshInstalledVersion();

        updatePageStatusText = new TextView(this);
        updatePageStatusText.setText(updateStatusSummary);
        updatePageStatusText.setTextColor(MUTED);
        updatePageStatusText.setTextSize(14);
        updatePageStatusText.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams updateStatusParams = params(-1, -2);
        updateStatusParams.topMargin = dp(16);
        infoCard.addView(updatePageStatusText, updateStatusParams);

        updatePageButton = actionButton(tr("Check for updates", "التحقق من التحديثات"), ACCENT, Color.rgb(4, 26, 28), ACCENT, 15);
        updatePageButton.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        updatePageButton.setEnabled(true);
        LinearLayout.LayoutParams updateButtonParams = params(-1, dp(52));
        updateButtonParams.topMargin = dp(17);
        infoCard.addView(updatePageButton, updateButtonParams);
        updatePageButton.setOnClickListener(v -> checkForYtDlpUpdate(updatePageStatusText, updatePageButton));

        TextView explanation = new TextView(this);
        explanation.setText(tr("The 90-day message indicates an old yt-dlp build. ClipNest attempts an update and one retry when it detects that error. The --no-update option only hides the warning; it does not update yt-dlp.", "تعني رسالة 90 يومًا أن إصدار yt-dlp قديم. يحاول ClipNest التحديث وإعادة المحاولة مرة واحدة عند اكتشاف الخطأ. خيار --no-update يخفي التحذير فقط ولا يحدّث yt-dlp."));
        explanation.setTextColor(MUTED);
        explanation.setTextSize(13);
        explanation.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams explanationParams = params(-1, -2);
        explanationParams.topMargin = dp(18);
        page.addView(explanation, explanationParams);

        LinearLayout galleryUpdateCard = new LinearLayout(this);
        galleryUpdateCard.setOrientation(LinearLayout.VERTICAL);
        galleryUpdateCard.setPadding(dp(18), dp(18), dp(18), dp(18));
        galleryUpdateCard.setBackground(rounded(PANEL, 20));
        LinearLayout.LayoutParams galleryUpdateCardParams = params(-1, -2);
        galleryUpdateCardParams.topMargin = dp(22);
        page.addView(galleryUpdateCard, galleryUpdateCardParams);
        galleryUpdateCard.addView(label(tr("GALLERY-DL ENGINE", "محرّك Gallery-dl")));
        TextView galleryUpdateTitle = new TextView(this);
        galleryUpdateTitle.setText(tr("Gallery-dl updates", "تحديثات Gallery-dl"));
        galleryUpdateTitle.setTextColor(TEXT);
        galleryUpdateTitle.setTextSize(22);
        galleryUpdateTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams galleryUpdateTitleParams = params(-1, -2);
        galleryUpdateTitleParams.topMargin = dp(8);
        galleryUpdateCard.addView(galleryUpdateTitle, galleryUpdateTitleParams);
        TextView galleryChannelLabel = label(tr("UPDATE CHANNEL", "قناة التحديث"));
        LinearLayout.LayoutParams galleryChannelLabelParams = params(-1, -2);
        galleryChannelLabelParams.topMargin = dp(18);
        galleryUpdateCard.addView(galleryChannelLabel, galleryChannelLabelParams);
        TextView galleryChannelValue = new TextView(this);
        galleryChannelValue.setText(tr("PyPI · official Gallery-dl release", "PyPI · إصدار Gallery-dl الرسمي"));
        galleryChannelValue.setTextColor(TEXT);
        galleryChannelValue.setTextSize(15);
        LinearLayout.LayoutParams galleryChannelValueParams = params(-1, -2);
        galleryChannelValueParams.topMargin = dp(7);
        galleryUpdateCard.addView(galleryChannelValue, galleryChannelValueParams);
        TextView galleryInstalledLabel = label(tr("INSTALLED VERSION", "الإصدار المثبّت"));
        LinearLayout.LayoutParams galleryInstalledLabelParams = params(-1, -2);
        galleryInstalledLabelParams.topMargin = dp(18);
        galleryUpdateCard.addView(galleryInstalledLabel, galleryInstalledLabelParams);
        galleryVersionText = new TextView(this);
        galleryVersionText.setTextColor(ACCENT);
        galleryVersionText.setTextSize(14);
        LinearLayout.LayoutParams galleryVersionParams = params(-1, -2);
        galleryVersionParams.topMargin = dp(8);
        galleryUpdateCard.addView(galleryVersionText, galleryVersionParams);
        galleryUpdateStatusText = new TextView(this);
        galleryUpdateStatusText.setTextColor(MUTED);
        galleryUpdateStatusText.setTextSize(14);
        galleryUpdateStatusText.setTextIsSelectable(true);
        galleryUpdateStatusText.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams galleryStatusParams = params(-1, -2);
        galleryStatusParams.topMargin = dp(12);
        galleryUpdateCard.addView(galleryUpdateStatusText, galleryStatusParams);
        galleryUpdateButton = actionButton(tr("Check and update Gallery-dl", "التحقق من Gallery-dl وتحديثه"), Color.rgb(26, 54, 56), ACCENT, Color.rgb(45, 104, 103), 14);
        LinearLayout.LayoutParams galleryUpdateButtonParams = params(-1, dp(50));
        galleryUpdateButtonParams.topMargin = dp(15);
        galleryUpdateCard.addView(galleryUpdateButton, galleryUpdateButtonParams);
        refreshGalleryVersion();
        galleryUpdateButton.setOnClickListener(v -> checkForGalleryUpdate());

        return scroll;
    }

    private View buildBrowserPage() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(16), dp(16), dp(16), dp(12));
        page.setBackgroundColor(BG);

        TextView title = new TextView(this);
        title.setText(tr("Public browser", "المتصفح العام"));
        title.setTextColor(TEXT);
        title.setTextSize(25);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        page.addView(title);

        browserNoteText = new TextView(this);
        browserNoteText.setTextColor(MUTED);
        browserNoteText.setTextSize(12);
        browserNoteText.setLineSpacing(dp(2), 1f);
        browserNoteText.setVisibility(View.GONE);

        LinearLayout addressRow = new LinearLayout(this);
        addressRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams addressRowParams = params(-1, dp(50));
        addressRowParams.topMargin = dp(8);

        browserAddressInput = new EditText(this);
        browserAddressInput.setSingleLine(true);
        browserAddressInput.setText("https://www.youtube.com");
        browserAddressInput.setHint(tr("Search DuckDuckGo or enter a URL", "ابحث في DuckDuckGo أو أدخل رابطًا"));
        browserAddressInput.setHintTextColor(MUTED);
        browserAddressInput.setTextColor(TEXT);
        browserAddressInput.setTextSize(13);
        browserAddressInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        browserAddressInput.setPadding(dp(13), 0, dp(13), 0);
        GradientDrawable addressBackground = rounded(Color.rgb(12, 15, 20), 14);
        addressBackground.setStroke(dp(1), Color.rgb(65, 86, 96));
        browserAddressInput.setBackground(addressBackground);
        browserAddressInput.setClipToOutline(true);
        LinearLayout.LayoutParams addressInputParams = new LinearLayout.LayoutParams(0, -1, 1f);
        addressInputParams.rightMargin = dp(4);
        addressInputParams.leftMargin = dp(2);
        addressRow.addView(browserAddressInput, addressInputParams);

        Button goButton = actionButton("↗", Color.rgb(26, 54, 56), ACCENT, Color.rgb(45, 104, 103), 21);
        goButton.setContentDescription(tr("Search or open", "بحث أو فتح"));
        LinearLayout.LayoutParams goParams = params(dp(48), dp(46));
        goParams.leftMargin = dp(8);
        addressRow.addView(goButton, goParams);

        Button bookmarkButton = actionButton("☆", Color.rgb(26, 39, 48), ACCENT, Color.rgb(50, 73, 82), 20);
        bookmarkButton.setContentDescription(tr("Add bookmark", "إضافة إشارة مرجعية"));
        LinearLayout.LayoutParams bookmarkButtonParams = params(dp(48), dp(46));
        bookmarkButtonParams.leftMargin = dp(8);
        addressRow.addView(bookmarkButton, bookmarkButtonParams);

        bookmarkListButton = actionButton("", Color.rgb(26, 39, 48), ACCENT, Color.rgb(50, 73, 82), 12);
        bookmarkListButton.setContentDescription(tr("Open bookmarks", "فتح الإشارات المرجعية"));
        renderBookmarkButton();

        browserProgress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        browserProgress.setMax(100);
        browserProgress.setProgressTintList(ColorStateList.valueOf(ACCENT));
        browserProgress.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(48, 56, 66)));
        page.addView(browserProgress, params(-1, dp(3)));

        browserWebView = new WebView(this);
        browserWebView.setBackgroundColor(Color.WHITE);
        browserWebView.setSaveEnabled(false);
        applyWebBrowserSettings();
        browserWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (!request.isForMainFrame()) return false;
                if (!isHttpUrl(uri)) {
                    browserStatusText.setText(tr("Only http and https pages can be opened here.", "يمكن فتح صفحات http وhttps فقط هنا."));
                    return true;
                }
                browserAddressInput.setText(uri.toString());
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                browserAddressInput.setText(url);
                browserStatusText.setText(tr("Loading public page…", "جارٍ تحميل الصفحة العامة…"));
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                browserProgress.setProgress(100);
                browserStatusText.setText(tr("YouTube sign-in is available · cookies stay local until you export them", "تسجيل الدخول إلى YouTube متاح · تبقى ملفات الارتباط محلية حتى تصدّرها"));
            }
        });
        browserWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                browserProgress.setProgress(newProgress);
            }
        });
        browserWebView.setOnLongClickListener(v -> handleWebLongPress(browserWebView.getHitTestResult()));
        LinearLayout.LayoutParams webParams = new LinearLayout.LayoutParams(-1, 0, 1f);
        webParams.topMargin = dp(8);
        webParams.leftMargin = -dp(16);
        webParams.rightMargin = -dp(16);
        webParams.bottomMargin = -dp(12);
        page.addView(browserWebView, webParams);

        LinearLayout.LayoutParams bookmarkListParams = params(dp(48), dp(46));
        bookmarkListParams.leftMargin = dp(8);
        addressRow.addView(bookmarkListButton, bookmarkListParams);
        page.addView(addressRow, addressRowParams);

        browserStatusText = new TextView(this);
        browserStatusText.setText(tr("YouTube sign-in is available · cookies stay local until you export them", "تسجيل الدخول إلى YouTube متاح · تبقى ملفات الارتباط محلية حتى تصدّرها"));
        browserStatusText.setTextColor(MUTED);
        browserStatusText.setTextSize(11);
        browserStatusText.setPadding(dp(2), dp(6), dp(2), 0);
        goButton.setOnClickListener(v -> loadBrowserAddress());
        bookmarkButton.setOnClickListener(v -> addCurrentBookmark());
        bookmarkListButton.setOnClickListener(v -> showBookmarksDialog());
        browserAddressInput.setOnEditorActionListener((view, actionId, event) -> {
            loadBrowserAddress();
            return true;
        });
        updateBrowserPrivacyNote();
        return page;
    }

    private ScrollView buildWebConfigPage() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(24), dp(26), dp(24), dp(28));
        scroll.addView(page);

        page.addView(label(tr("BROWSER PREFERENCES", "تفضيلات المتصفح")));
        TextView title = new TextView(this);
        title.setText(tr("Web config", "إعدادات الويب"));
        title.setTextColor(TEXT);
        title.setTextSize(30);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = params(-1, -2);
        titleParams.topMargin = dp(10);
        page.addView(title, titleParams);

        TextView intro = new TextView(this);
        intro.setText(tr("Choose which features the in-app browser may use. These settings are saved on this device and affect the Browser tab only.", "اختر ميزات المتصفح داخل التطبيق. تُحفظ هذه الإعدادات على هذا الجهاز وتؤثر على تبويب المتصفح فقط."));
        intro.setTextColor(MUTED);
        intro.setTextSize(14);
        intro.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams introParams = params(-1, -2);
        introParams.topMargin = dp(8);
        page.addView(intro, introParams);

        Button languageButton = actionButton(
            tr("English  ->  العربية", "العربية  ->  English"),
                Color.rgb(26, 39, 48), ACCENT, Color.rgb(50, 73, 82), 13);
        LinearLayout.LayoutParams languageParams = params(-1, dp(46));
        languageParams.topMargin = dp(16);
        page.addView(languageButton, languageParams);
        languageButton.setOnClickListener(v -> {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString(PREF_LANGUAGE, isArabic() ? "en" : "ar").apply();
            recreate();
        });

        batteryButton = actionButton(
                tr("Battery optimization: checking…", "تحسين البطارية: جارٍ التحقق…"),
                Color.rgb(26, 54, 56), ACCENT, Color.rgb(45, 104, 103), 13);
        LinearLayout.LayoutParams batteryParams = params(-1, dp(48));
        batteryParams.topMargin = dp(9);
        page.addView(batteryButton, batteryParams);
        batteryStatusText = new TextView(this);
        batteryStatusText.setTextColor(MUTED);
        batteryStatusText.setTextSize(11);
        LinearLayout.LayoutParams batteryStatusParams = params(-1, -2);
        batteryStatusParams.topMargin = dp(5);
        page.addView(batteryStatusText, batteryStatusParams);
        Runnable renderBatteryStatus = () -> batteryStatusText.setText(isBatteryOptimizationIgnored()
                ? tr("Battery optimization is disabled for ClipNest.", "تم تعطيل تحسين البطارية لتطبيق ClipNest.")
                : tr("Android may pause background work. Tap the button and approve the system request.", "قد يوقف Android العمل في الخلفية. اضغط الزر ووافق على طلب النظام."));
        renderBatteryStatus.run();
        renderBatteryButton();
        batteryButton.setOnClickListener(v -> requestBatteryOptimizationExemption());

        LinearLayout settings = new LinearLayout(this);
        settings.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams settingsParams = params(-1, -2);
        settingsParams.topMargin = dp(18);
        page.addView(settings, settingsParams);
        addWebSetting(settings, tr("JavaScript", "جافاسكريبت"), tr("Allow scripts used by interactive websites.", "السماح بالبرامج النصية التي تستخدمها المواقع التفاعلية."), PREF_WEB_JS, true);
        addWebSetting(settings, tr("First-party cookies", "ملفات تعريف الارتباط الأساسية"), tr("Allow site cookies inside the WebView. Required for Google/YouTube sign-in; cookies are passed to yt-dlp only after you explicitly export them.", "السماح بملفات تعريف المواقع داخل WebView. مطلوبة لتسجيل الدخول إلى Google/YouTube؛ لا تُرسل إلى yt-dlp إلا بعد تصديرها صراحةً."), PREF_WEB_COOKIES, false);
        addWebSetting(settings, tr("Third-party cookies", "ملفات تعريف الارتباط للطرف الثالث"), tr("Allow cookies from embedded or cross-site content. This can increase cross-site tracking and may be required by Google sign-in.", "السماح بملفات تعريف الارتباط من المحتوى المضمّن أو المواقع الأخرى. قد يزيد ذلك التتبع بين المواقع وقد يكون مطلوبًا لتسجيل الدخول إلى Google."), PREF_WEB_THIRD_PARTY_COOKIES, false);
        addWebSetting(settings, tr("DOM storage", "تخزين DOM"), tr("Allow websites to store local browser data separately from cookies.", "السماح للمواقع بتخزين بيانات المتصفح المحلية بشكل منفصل عن ملفات تعريف الارتباط."), PREF_WEB_DOM_STORAGE, false);
        addWebSetting(settings, tr("Zoom controls", "عناصر التحكم بالتكبير"), tr("Enable pinch-to-zoom in the Browser tab.", "تفعيل التكبير باللمس في تبويب المتصفح."), PREF_WEB_ZOOM, false);
        addWebSetting(settings, tr("Button animations", "حركات الأزرار"), tr("Show a short ripple effect when buttons are pressed.", "إظهار تأثير قصير عند الضغط على الأزرار."), PREF_ANIMATIONS, true);
        addWebSetting(settings, tr("Keep screen on during downloads", "إبقاء الشاشة قيد التشغيل أثناء التنزيل"), tr("Prevent the screen from sleeping while a yt-dlp download is running.", "منع الشاشة من الدخول في وضع السكون أثناء تشغيل تنزيل yt-dlp."), PREF_KEEP_SCREEN_ON, true);

        TextView cookieTitle = new TextView(this);
        cookieTitle.setText(tr("YouTube cookies for yt-dlp", "ملفات YouTube لـ yt-dlp"));
        cookieTitle.setTextColor(TEXT);
        cookieTitle.setTextSize(15);
        cookieTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams cookieTitleParams = params(-1, -2);
        cookieTitleParams.topMargin = dp(10);
        page.addView(cookieTitle, cookieTitleParams);
        TextView cookieHelp = new TextView(this);
        cookieHelp.setText(tr("Sign in directly inside the Browser tab. When you are done, come here to export the WebView session, or import a user-owned Netscape cookies.txt file.", "سجّل الدخول مباشرةً من تبويب المتصفح. بعد الانتهاء، عد إلى هنا لتصدير جلسة WebView أو استيراد ملف cookies.txt بصيغة Netscape تملكه."));
        cookieHelp.setTextColor(MUTED);
        cookieHelp.setTextSize(12);
        cookieHelp.setLineSpacing(dp(2), 1f);
        LinearLayout.LayoutParams cookieHelpParams = params(-1, -2);
        cookieHelpParams.topMargin = dp(5);
        page.addView(cookieHelp, cookieHelpParams);

        Button cookieGuideButton = actionButton(tr("How cookies & WebView work", "كيف تعمل ملفات الارتباط وWebView"), Color.rgb(26, 39, 48), ACCENT, Color.rgb(50, 73, 82), 12);
        LinearLayout.LayoutParams cookieGuideParams = params(-1, dp(42));
        cookieGuideParams.topMargin = dp(9);
        page.addView(cookieGuideButton, cookieGuideParams);
        cookieGuideButton.setOnClickListener(v -> showSimpleInfoDialog(
                tr("Cookies & WebView", "ملفات الارتباط وWebView"),
                tr("1. Enable cookies and DOM storage.\n2. Sign in inside Browser.\n3. Export cookies, then download.\n\nCookies stay local unless you export them. Treat cookies.txt like a password.",
                        "١. فعّل ملفات الارتباط وتخزين DOM.\n٢. سجّل الدخول من المتصفح.\n٣. صدّر ملفات الارتباط ثم نزّل.\n\nتبقى ملفات الارتباط محلية ما لم تصدّرها. تعامل مع cookies.txt ككلمة مرور.")));

        LinearLayout cookieActions = new LinearLayout(this);
        cookieActions.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams cookieActionsParams = params(-1, dp(48));
        cookieActionsParams.topMargin = dp(10);
        page.addView(cookieActions, cookieActionsParams);
        Button exportCookiesButton = actionButton(tr("Export cookies", "تصدير ملفات الارتباط"), Color.rgb(26, 54, 56), ACCENT, Color.rgb(45, 104, 103), 13);
        LinearLayout.LayoutParams exportCookieParams = params(0, -1);
        exportCookieParams.weight = 1f;
        exportCookieParams.rightMargin = dp(8);
        cookieActions.addView(exportCookiesButton, exportCookieParams);
        Button importCookiesButton = actionButton(tr("Import cookies.txt", "استيراد cookies.txt"), Color.rgb(26, 39, 48), ACCENT, Color.rgb(50, 73, 82), 13);
        LinearLayout.LayoutParams importCookieParams = params(0, -1);
        importCookieParams.leftMargin = dp(8);
        importCookieParams.weight = 1f;
        cookieActions.addView(importCookiesButton, importCookieParams);
        cookieStatusText = new TextView(this);
        cookieStatusText.setTextColor(MUTED);
        cookieStatusText.setTextSize(11);
        LinearLayout.LayoutParams cookieStatusParams = params(-1, -2);
        cookieStatusParams.topMargin = dp(7);
        page.addView(cookieStatusText, cookieStatusParams);
        exportCookiesButton.setOnClickListener(v -> exportWebViewCookies());
        importCookiesButton.setOnClickListener(v -> chooseCookieFile());
        refreshCookieStatus();

        Button clearData = actionButton(tr("Clear browser cookies & data", "مسح ملفات تعريف وبيانات المتصفح"), Color.rgb(54, 27, 34), DANGER, Color.rgb(119, 56, 66), 14);
        LinearLayout.LayoutParams clearParams = params(-1, dp(50));
        clearParams.topMargin = dp(8);
        page.addView(clearData, clearParams);
        clearData.setOnClickListener(v -> clearBrowserData());

        webConfigStatusText = new TextView(this);
        webConfigStatusText.setText(tr("Settings apply immediately to new and current pages.", "تُطبّق الإعدادات فورًا على الصفحات الجديدة والحالية."));
        webConfigStatusText.setTextColor(MUTED);
        webConfigStatusText.setTextSize(12);
        LinearLayout.LayoutParams statusParams = params(-1, -2);
        statusParams.topMargin = dp(12);
        page.addView(webConfigStatusText, statusParams);

        TextView privacy = new TextView(this);
        privacy.setText(tr("Privacy: YouTube/Google sign-in is available in the WebView. Cookies remain local unless you explicitly export them or import a user-owned cookies.txt file. Cookie files can access your account; keep them private.", "الخصوصية: تسجيل الدخول إلى YouTube/Google متاح داخل WebView. تبقى ملفات تعريف الارتباط محلية ما لم تصدّرها أو تستورد ملف cookies.txt تملكه. يمكن لملفات الارتباط الوصول إلى حسابك؛ احتفظ بها خاصة."));
        privacy.setTextColor(MUTED);
        privacy.setTextSize(12);
        privacy.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams privacyParams = params(-1, -2);
        privacyParams.topMargin = dp(18);
        page.addView(privacy, privacyParams);
        addCreditsSection(page);
        return scroll;
    }

    private void addCreditsSection(LinearLayout page) {
        TextView heading = new TextView(this);
        heading.setText(tr("Credits & licenses", "الاعتمادات والتراخيص"));
        heading.setTextColor(ACCENT);
        heading.setTextSize(16);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams headingParams = params(-1, -2);
        headingParams.topMargin = dp(26);
        page.addView(heading, headingParams);
        TextView credits = new TextView(this);
        credits.setText(tr(
                "Requested by: M7mdThaer\n\nClipNest app source: MIT License.\n\nDownload engine: yt-dlp, an independent open-source project. yt-dlp copyrights and license belong to its contributors and upstream project; ClipNest does not claim ownership of yt-dlp.\n\nMedia processing: FFmpeg, distributed under its own upstream license. Third-party notices are included with the source.",
                "طُلب إنشاء التطبيق بواسطة: M7mdThaer\n\nمصدر تطبيق ClipNest: مرخّص بموجب MIT.\n\nمحرّك التنزيل: yt-dlp، وهو مشروع مستقل مفتوح المصدر. حقوق الطبع والنشر وترخيص yt-dlp تعود إلى مساهميه ومشروعه الأصلي؛ لا يدّعي ClipNest ملكية yt-dlp.\n\nمعالجة الوسائط: FFmpeg، ويخضع لترخيصه الأصلي الخاص. أُدرجت إشعارات الأطراف الثالثة مع المصدر."));
        credits.setTextColor(MUTED);
        credits.setTextSize(12);
        credits.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams creditsParams = params(-1, -2);
        creditsParams.topMargin = dp(9);
        page.addView(credits, creditsParams);

        Button privacyButton = actionButton(tr("Privacy & copyright", "الخصوصية وحقوق النشر"), Color.rgb(26, 39, 48), ACCENT, Color.rgb(50, 73, 82), 12);
        LinearLayout.LayoutParams privacyButtonParams = params(-1, dp(42));
        privacyButtonParams.topMargin = dp(12);
        page.addView(privacyButton, privacyButtonParams);
        privacyButton.setOnClickListener(v -> showSimpleInfoDialog(
                tr("Privacy & copyright", "الخصوصية وحقوق النشر"),
                tr("ClipNest is released under the MIT License. Copyright (c) 2026 M7mdThaer.\n\nCookies and browser data stay on your device unless you explicitly export or import them. You control what you download and must respect applicable rights and site terms.",
                        "يصدر ClipNest بموجب ترخيص MIT. حقوق النشر (c) 2026 M7mdThaer.\n\nتبقى ملفات الارتباط وبيانات المتصفح على جهازك ما لم تصدّرها أو تستوردها صراحةً. أنت تتحكم فيما تنزّله ويجب احترام الحقوق وشروط المواقع.")));

        Button ytdlpButton = actionButton("yt-dlp", Color.rgb(26, 39, 48), ACCENT, Color.rgb(50, 73, 82), 12);
        LinearLayout.LayoutParams ytdlpButtonParams = params(-1, dp(42));
        ytdlpButtonParams.topMargin = dp(8);
        page.addView(ytdlpButton, ytdlpButtonParams);
        ytdlpButton.setOnClickListener(v -> showSimpleInfoDialog("yt-dlp", tr(
                "yt-dlp is an independent open-source download project. Its upstream contributors retain their copyrights and licensing terms. ClipNest uses it as a separate component and does not claim ownership.",
                "yt-dlp مشروع مستقل مفتوح المصدر للتنزيل. يحتفظ المساهمون في المشروع الأصلي بحقوق النشر وشروط الترخيص الخاصة بهم. يستخدمه ClipNest كمكوّن منفصل ولا يدّعي ملكيته.")));

        Button galleryCreditButton = actionButton("Gallery-dl", Color.rgb(26, 39, 48), ACCENT, Color.rgb(50, 73, 82), 12);
        LinearLayout.LayoutParams galleryCreditParams = params(-1, dp(42));
        galleryCreditParams.topMargin = dp(8);
        page.addView(galleryCreditButton, galleryCreditParams);
        galleryCreditButton.setOnClickListener(v -> showSimpleInfoDialog("Gallery-dl", tr(
                "Gallery-dl is an independent open-source image and gallery downloader. Its upstream contributors retain their copyrights and licensing terms. ClipNest bundles it as a separate component and does not claim ownership.",
                "Gallery-dl أداة مستقلة مفتوحة المصدر لتنزيل الصور والمعارض. يحتفظ المساهمون في المشروع الأصلي بحقوق النشر وشروط الترخيص الخاصة بهم. يضمّه ClipNest كمكوّن منفصل ولا يدّعي ملكيته.")));

        Button ffmpegButton = actionButton("FFmpeg", Color.rgb(26, 39, 48), ACCENT, Color.rgb(50, 73, 82), 12);
        LinearLayout.LayoutParams ffmpegButtonParams = params(-1, dp(42));
        ffmpegButtonParams.topMargin = dp(8);
        page.addView(ffmpegButton, ffmpegButtonParams);
        ffmpegButton.setOnClickListener(v -> showSimpleInfoDialog("FFmpeg", tr(
                "FFmpeg is a separate multimedia framework used for merging and audio conversion. It is distributed under its own upstream license and notices.",
                "FFmpeg إطار وسائط منفصل يُستخدم للدمج وتحويل الصوت. يوزّع بموجب ترخيصه وإشعاراته الأصلية الخاصة.")));

        Button galleryPrivacyButton = actionButton("Gallery-dl privacy", Color.rgb(26, 39, 48), ACCENT, Color.rgb(50, 73, 82), 12);
        LinearLayout.LayoutParams galleryPrivacyParams = params(-1, dp(42));
        galleryPrivacyParams.topMargin = dp(8);
        page.addView(galleryPrivacyButton, galleryPrivacyParams);
        galleryPrivacyButton.setOnClickListener(v -> showSimpleInfoDialog(
                tr("Gallery-dl privacy", "خصوصية Gallery-dl"),
                tr("Gallery-dl runs locally inside ClipNest. URLs, downloaded files, and the selected destination are handled on this device. The source website may still receive normal requests and its own cookies or privacy rules apply. Use only links and media you are authorized to download.",
                        "يعمل Gallery-dl محليًا داخل ClipNest. تتم معالجة الروابط والملفات ومسار الحفظ على هذا الجهاز. قد يستقبل الموقع المصدر الطلبات المعتادة وتطبّق ملفات تعريف الارتباط وسياسة الخصوصية الخاصة به. استخدم فقط الروابط والوسائط المصرّح لك بتنزيلها.")));
    }

    private void showSimpleInfoDialog(String titleText, String bodyText) {
        Dialog dialog = new Dialog(this);
        LinearLayout card = dialogCard();
        TextView title = new TextView(this);
        title.setText(titleText);
        title.setTextColor(TEXT);
        title.setTextSize(21);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(title);
        TextView body = new TextView(this);
        body.setText(bodyText);
        body.setTextColor(MUTED);
        body.setTextSize(13);
        body.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams bodyParams = params(-1, -2);
        bodyParams.topMargin = dp(12);
        card.addView(body, bodyParams);
        Button close = actionButton(tr("Close", "إغلاق"), Color.rgb(38, 44, 52), TEXT, Color.rgb(58, 66, 76), 13);
        LinearLayout.LayoutParams closeParams = params(-1, dp(44));
        closeParams.topMargin = dp(16);
        card.addView(close, closeParams);
        close.setOnClickListener(v -> dialog.dismiss());
        configureCardDialog(dialog, card, false);
    }

    private boolean isBatteryOptimizationIgnored() {
        if (android.os.Build.VERSION.SDK_INT < 23) return true;
        android.os.PowerManager powerManager = (android.os.PowerManager) getSystemService(Context.POWER_SERVICE);
        return powerManager != null && powerManager.isIgnoringBatteryOptimizations(getPackageName());
    }

    private void renderBatteryButton() {
        if (batteryButton == null) return;
        batteryButton.setText(isBatteryOptimizationIgnored()
                ? tr("Battery optimization disabled ✓", "تم تعطيل تحسين البطارية ✓")
                : tr("Disable battery optimization", "تعطيل تحسين البطارية"));
    }

    private void requestBatteryOptimizationExemption() {
        if (isBatteryOptimizationIgnored()) {
            Toast.makeText(this, tr("Battery optimization is already disabled for ClipNest.", "تم تعطيل تحسين البطارية مسبقًا لتطبيق ClipNest."), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
            } catch (Exception ignored) {
                Toast.makeText(this, tr("Open Android battery settings and allow ClipNest to run unrestricted.", "افتح إعدادات بطارية Android واسمح لتطبيق ClipNest بالعمل دون قيود."), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addWebSetting(LinearLayout parent, String title, String description, String preferenceKey, boolean defaultValue) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(16), dp(16), dp(16));
        row.setBackground(rounded(PANEL, 17));
        row.setClipToOutline(true);
        LinearLayout.LayoutParams rowParams = params(-1, -2);
        rowParams.bottomMargin = dp(9);
        parent.addView(row, rowParams);

        LinearLayout textColumn = new LinearLayout(this);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        row.addView(textColumn, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView titleText = new TextView(this);
        titleText.setText(title);
        titleText.setTextColor(TEXT);
        titleText.setTextSize(14);
        titleText.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        textColumn.addView(titleText);
        TextView descriptionText = new TextView(this);
        descriptionText.setText(description);
        descriptionText.setTextColor(MUTED);
        descriptionText.setTextSize(11);
        descriptionText.setLineSpacing(dp(2), 1f);
        LinearLayout.LayoutParams descriptionParams = params(-1, -2);
        descriptionParams.topMargin = dp(4);
        textColumn.addView(descriptionText, descriptionParams);

        TextView state = new TextView(this);
        state.setGravity(Gravity.CENTER);
        state.setTextSize(11);
        state.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        LinearLayout.LayoutParams stateParams = params(dp(52), dp(32));
        stateParams.leftMargin = dp(16);
        row.addView(state, stateParams);

        SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        Runnable renderState = () -> {
            boolean enabled = preferences.getBoolean(preferenceKey, defaultValue);
            state.setText(enabled ? "ON" : "OFF");
            state.setTextColor(enabled ? Color.rgb(4, 26, 28) : MUTED);
            state.setBackground(rounded(enabled ? ACCENT : Color.rgb(38, 44, 52), 11));
            state.setClipToOutline(true);
        };
        renderState.run();
        row.setOnClickListener(v -> {
            boolean enabled = !preferences.getBoolean(preferenceKey, defaultValue);
            preferences.edit().putBoolean(preferenceKey, enabled).apply();
            if (PREF_ANIMATIONS.equals(preferenceKey)) {
                recreate();
                return;
            }
            renderState.run();
            applyWebBrowserSettings();
            webConfigStatusText.setText(title + (enabled ? " enabled." : " disabled."));
        });
    }

    private void applyWebBrowserSettings() {
        SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean acceptCookies = preferences.getBoolean(PREF_WEB_COOKIES, false);
        boolean acceptThirdPartyCookies = preferences.getBoolean(PREF_WEB_THIRD_PARTY_COOKIES, false);
        if (browserWebView != null) {
            android.webkit.WebSettings settings = browserWebView.getSettings();
            settings.setJavaScriptEnabled(preferences.getBoolean(PREF_WEB_JS, true));
            settings.setDomStorageEnabled(preferences.getBoolean(PREF_WEB_DOM_STORAGE, false));
            boolean zoom = preferences.getBoolean(PREF_WEB_ZOOM, false);
            settings.setSupportZoom(zoom);
            settings.setBuiltInZoomControls(zoom);
            settings.setDisplayZoomControls(false);
            settings.setAllowFileAccess(false);
            settings.setAllowContentAccess(false);
            settings.setJavaScriptCanOpenWindowsAutomatically(false);
            settings.setSupportMultipleWindows(false);
            settings.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        }
        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(acceptCookies);
        if (browserWebView != null) cookies.setAcceptThirdPartyCookies(browserWebView, acceptThirdPartyCookies);
        if (!acceptCookies) {
            cookies.removeAllCookies(null);
            cookies.flush();
        }
        updateBrowserPrivacyNote();
    }

    private void updateBrowserPrivacyNote() {
        if (browserNoteText == null) return;
        SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean acceptCookies = preferences.getBoolean(PREF_WEB_COOKIES, false);
        boolean acceptThirdPartyCookies = preferences.getBoolean(PREF_WEB_THIRD_PARTY_COOKIES, false);
        String thirdPartyState = !acceptThirdPartyCookies ? tr("third-party off", "الطرف الثالث متوقف")
                : (acceptCookies ? tr("third-party on", "الطرف الثالث يعمل") : tr("third-party on, inactive while cookies are off", "الطرف الثالث يعمل لكنه غير نشط مع إيقاف ملفات الارتباط"));
        browserNoteText.setText((acceptCookies ? tr("First-party cookies on", "ملفات الارتباط الأساسية مفعّلة") : tr("Cookies off", "ملفات الارتباط متوقفة"))
                + " · " + thirdPartyState
                + tr(". YouTube sign-in is allowed when cookies are enabled; browser data is only sent to yt-dlp after you export or import cookies.txt.", ". يُسمح بتسجيل الدخول إلى YouTube عند تفعيل ملفات الارتباط؛ لا تُرسل بيانات المتصفح إلى yt-dlp إلا بعد تصدير أو استيراد cookies.txt."));
    }

    private void clearBrowserData() {
        if (browserWebView != null) {
            browserWebView.clearCache(true);
            browserWebView.clearHistory();
            browserWebView.clearFormData();
        }
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(removed -> {
            CookieManager.getInstance().flush();
            main.post(() -> {
                if (webConfigStatusText != null) webConfigStatusText.setText("Browser cookies, site storage, cache, and history cleared.");
                if (browserStatusText != null) browserStatusText.setText("Browser data cleared.");
            });
        });
    }

    private void restoreBookmarks() {
        bookmarks.clear();
        String saved = getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_BOOKMARKS, "");
        if (saved == null || saved.trim().isEmpty()) return;
        for (String value : saved.split("\\n")) {
            String url = value.trim();
            if (isHttpUrl(Uri.parse(url)) && !bookmarks.contains(url)) bookmarks.add(url);
        }
    }

    private void saveBookmarks() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putString(PREF_BOOKMARKS, String.join("\n", bookmarks)).apply();
    }

    private void addCurrentBookmark() {
        String url = browserWebView == null ? browserAddressInput.getText().toString().trim() : browserWebView.getUrl();
        if (url == null || !isHttpUrl(Uri.parse(url))) {
            Toast.makeText(this, tr("Open a valid web page first.", "افتح صفحة ويب صالحة أولًا."), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bookmarks.contains(url)) {
            bookmarks.add(url);
            saveBookmarks();
            renderBookmarkButton();
        }
        Toast.makeText(this, tr("Bookmark saved.", "تم حفظ الإشارة المرجعية."), Toast.LENGTH_SHORT).show();
    }

    private void renderBookmarkButton() {
        if (bookmarkListButton == null) return;
        bookmarkListButton.setText(bookmarks.isEmpty()
                ? tr("☆ Bookmarks", "☆ الإشارات المرجعية")
                : tr("★ Bookmarks (" + bookmarks.size() + ")", "★ الإشارات المرجعية (" + bookmarks.size() + ")"));
    }

    private void showBookmarksDialog() {
        Dialog dialog = new Dialog(this);
        LinearLayout card = dialogCard();
        TextView title = new TextView(this);
        title.setText(tr("Bookmarks", "الإشارات المرجعية"));
        title.setTextColor(TEXT);
        title.setTextSize(21);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(title);
        TextView help = new TextView(this);
        help.setText(tr("Tap to open. Long-press an item to select it, then delete it.", "اضغط للفتح. اضغط مطولًا لتحديد عنصر ثم احذفه."));
        help.setTextColor(MUTED);
        help.setTextSize(12);
        help.setLineSpacing(dp(2), 1f);
        LinearLayout.LayoutParams helpParams = params(-1, -2);
        helpParams.topMargin = dp(7);
        card.addView(help, helpParams);
        Button cancelSelection = actionButton(tr("Cancel selection", "إلغاء التحديد"), Color.rgb(38, 44, 52), TEXT, Color.rgb(58, 66, 76), 11);
        cancelSelection.setVisibility(View.GONE);
        LinearLayout.LayoutParams cancelSelectionParams = params(-1, dp(36));
        cancelSelectionParams.topMargin = dp(10);
        card.addView(cancelSelection, cancelSelectionParams);
        TextView selectionCount = new TextView(this);
        selectionCount.setText(tr("0 items selected", "تم تحديد 0 من العناصر"));
        selectionCount.setTextColor(MUTED);
        selectionCount.setTextSize(11);
        LinearLayout.LayoutParams selectionCountParams = params(-1, -2);
        selectionCountParams.topMargin = dp(6);
        card.addView(selectionCount, selectionCountParams);
        LinearLayout items = new LinearLayout(this);
        items.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams itemsParams = params(-1, -2);
        itemsParams.topMargin = dp(14);
        card.addView(items, itemsParams);
        final Set<Integer> selected = new HashSet<>();
        final List<Button> itemButtons = new ArrayList<>();
        Button delete = actionButton(tr("Delete selected", "حذف المحدد"), Color.rgb(54, 27, 34), DANGER, Color.rgb(119, 56, 66), 13);
        delete.setEnabled(false);
        delete.setVisibility(View.GONE);
        LinearLayout.LayoutParams deleteParams = params(-1, dp(44));
        deleteParams.topMargin = dp(12);
        card.addView(delete, deleteParams);
        for (int i = 0; i < bookmarks.size(); i++) {
            final int index = i;
            String url = bookmarks.get(i);
            Uri uri = Uri.parse(url);
            String label = uri.getHost() == null ? url : uri.getHost();
            Button item = actionButton(label, Color.rgb(26, 39, 48), ACCENT, Color.rgb(50, 73, 82), 12);
            item.setGravity(isArabic() ? Gravity.RIGHT | Gravity.CENTER_VERTICAL : Gravity.LEFT | Gravity.CENTER_VERTICAL);
            item.setSingleLine(true);
            item.setContentDescription(url);
            LinearLayout.LayoutParams itemParams = params(-1, dp(44));
            itemParams.bottomMargin = dp(8);
            items.addView(item, itemParams);
            itemButtons.add(item);
            item.setOnClickListener(v -> {
                if (!selected.isEmpty()) {
                    if (selected.contains(index)) selected.remove(index);
                    else selected.add(index);
                    item.setBackground(rounded(selected.contains(index) ? Color.rgb(30, 81, 79) : Color.rgb(26, 39, 48), 13));
                    selectionCount.setText(tr(selected.size() + " items selected", "تم تحديد " + selected.size() + " من العناصر"));
                    cancelSelection.setVisibility(selected.isEmpty() ? View.GONE : View.VISIBLE);
                    delete.setEnabled(!selected.isEmpty());
                    delete.setVisibility(selected.isEmpty() ? View.GONE : View.VISIBLE);
                } else {
                    browserWebView.loadUrl(url);
                    dialog.dismiss();
                }
            });
            item.setOnLongClickListener(v -> {
                if (selected.contains(index)) selected.remove(index);
                else selected.add(index);
                item.setBackground(rounded(selected.contains(index) ? Color.rgb(30, 81, 79) : Color.rgb(26, 39, 48), 13));
                selectionCount.setText(tr(selected.size() + " items selected", "تم تحديد " + selected.size() + " من العناصر"));
                cancelSelection.setVisibility(selected.isEmpty() ? View.GONE : View.VISIBLE);
                delete.setEnabled(!selected.isEmpty());
                delete.setVisibility(selected.isEmpty() ? View.GONE : View.VISIBLE);
                return true;
            });
        }
        cancelSelection.setOnClickListener(v -> {
            for (Button other : itemButtons) other.setBackground(rounded(Color.rgb(26, 39, 48), 13));
            selected.clear();
            selectionCount.setText(tr("0 items selected", "تم تحديد 0 من العناصر"));
            cancelSelection.setVisibility(View.GONE);
            delete.setEnabled(false);
            delete.setVisibility(View.GONE);
        });
        delete.setOnClickListener(v -> {
            if (!selected.isEmpty()) {
                List<Integer> indexes = new ArrayList<>(selected);
                indexes.sort(Collections.reverseOrder());
                for (int index : indexes) if (index >= 0 && index < bookmarks.size()) bookmarks.remove(index);
                saveBookmarks();
                renderBookmarkButton();
                dialog.dismiss();
                Toast.makeText(this, tr("Bookmark removed.", "تم حذف الإشارة المرجعية."), Toast.LENGTH_SHORT).show();
            }
        });
        Button close = actionButton(tr("Close", "إغلاق"), Color.rgb(38, 44, 52), TEXT, Color.rgb(58, 66, 76), 13);
        LinearLayout.LayoutParams closeParams = params(-1, dp(44));
        closeParams.topMargin = dp(8);
        card.addView(close, closeParams);
        close.setOnClickListener(v -> dialog.dismiss());
        dialog.setOnKeyListener((d, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && !selected.isEmpty()) {
                cancelSelection.performClick();
                return true;
            }
            return false;
        });
        configureCardDialog(dialog, card, false);
    }

    private boolean handleWebLongPress(WebView.HitTestResult hit) {
        if (hit == null) return false;
        int type = hit.getType();
        String extra = hit.getExtra();
        if (extra == null || extra.trim().isEmpty()) return false;
        if (type == WebView.HitTestResult.IMAGE_TYPE || type == WebView.HitTestResult.IMAGE_ANCHOR_TYPE) {
            showImageActionsDialog(extra);
            return true;
        }
        if (type == WebView.HitTestResult.SRC_ANCHOR_TYPE || type == WebView.HitTestResult.ANCHOR_TYPE) {
            copyToClipboard("Link", extra);
            Toast.makeText(this, tr("Link copied.", "تم نسخ الرابط."), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void showImageActionsDialog(String imageUrl) {
        Dialog dialog = new Dialog(this);
        LinearLayout card = dialogCard();
        TextView title = new TextView(this);
        title.setText(tr("Image actions", "إجراءات الصورة"));
        title.setTextColor(TEXT);
        title.setTextSize(20);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(title);
        Button download = actionButton(tr("Download image", "تنزيل الصورة"), Color.rgb(26, 54, 56), ACCENT, Color.rgb(45, 104, 103), 13);
        LinearLayout.LayoutParams downloadParams = params(-1, dp(46));
        downloadParams.topMargin = dp(14);
        card.addView(download, downloadParams);
        download.setOnClickListener(v -> {
            dialog.dismiss();
            downloadImage(imageUrl);
        });
        Button copy = actionButton(tr("Copy image URL", "نسخ رابط الصورة"), Color.rgb(26, 39, 48), ACCENT, Color.rgb(50, 73, 82), 13);
        LinearLayout.LayoutParams copyParams = params(-1, dp(46));
        copyParams.topMargin = dp(8);
        card.addView(copy, copyParams);
        copy.setOnClickListener(v -> {
            copyToClipboard("Image URL", imageUrl);
            dialog.dismiss();
        });
        Button close = actionButton(tr("Cancel", "إلغاء"), Color.rgb(38, 44, 52), TEXT, Color.rgb(58, 66, 76), 13);
        LinearLayout.LayoutParams closeParams = params(-1, dp(44));
        closeParams.topMargin = dp(8);
        card.addView(close, closeParams);
        close.setOnClickListener(v -> dialog.dismiss());
        configureCardDialog(dialog, card, false);
    }

    private void downloadImage(String imageUrl) {
        Uri imageUri = Uri.parse(imageUrl);
        String scheme = imageUri.getScheme() == null ? "" : imageUri.getScheme().toLowerCase(java.util.Locale.ROOT);
        if ("data".equals(scheme)) {
            worker.execute(() -> saveDataImage(imageUrl));
            return;
        }
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            downloadImageThroughWebView(imageUrl);
            return;
        }
        final Uri targetFolder = selectedFolderUri;
        final String webCookies = CookieManager.getInstance().getCookie(imageUrl);
        final String webUserAgent = browserWebView == null ? null : browserWebView.getSettings().getUserAgentString();
        worker.execute(() -> {
            try {
                Uri uri = Uri.parse(imageUrl);
                String filename = uri.getLastPathSegment();
                if (filename == null || filename.trim().isEmpty()) filename = "clipnest-image-" + System.currentTimeMillis() + ".jpg";
                filename = filename.replaceAll("[^A-Za-z0-9._-]", "_");
                HttpURLConnection connection = (HttpURLConnection) new URL(imageUrl).openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);
                if (webCookies != null && !webCookies.isEmpty()) connection.setRequestProperty("Cookie", webCookies);
                if (webUserAgent != null && !webUserAgent.isEmpty()) connection.setRequestProperty("User-Agent", webUserAgent);
                connection.connect();
                if (connection.getResponseCode() < 200 || connection.getResponseCode() >= 300) throw new IllegalStateException("Image server returned HTTP " + connection.getResponseCode());
                String contentType = connection.getContentType();
                byte[] bytes;
                try (InputStream input = connection.getInputStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                    copy(input, output);
                    bytes = output.toByteArray();
                } finally {
                    connection.disconnect();
                }
                Uri saved = saveImageBytes(bytes, filename, contentType, targetFolder);
                main.post(() -> Toast.makeText(this, tr("Image saved to the video destination.", "تم حفظ الصورة في مسار الفيديو."), Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                String message = safeMessage(e);
                main.post(() -> Toast.makeText(this, tr("Could not download image: ", "تعذر تنزيل الصورة: ") + message, Toast.LENGTH_LONG).show());
            }
        });
    }

    private void downloadImageThroughWebView(String imageUrl) {
        if (browserWebView == null) return;
        String script = "(async()=>{try{const r=await fetch(" + org.json.JSONObject.quote(imageUrl) + ");const b=await r.blob();const f=new FileReader();return await new Promise(ok=>{f.onloadend=()=>ok(f.result);f.readAsDataURL(b);});}catch(e){return 'CLIPCATCH_ERROR:'+e;}})()";
        browserWebView.evaluateJavascript(script, result -> {
            try {
                String data = new org.json.JSONTokener(result).nextValue().toString();
                if (data.startsWith("CLIPCATCH_ERROR:")) throw new IllegalStateException(data);
                worker.execute(() -> saveDataImage(data));
            } catch (Exception e) {
                Toast.makeText(this, tr("Could not read this image from the page.", "تعذر قراءة هذه الصورة من الصفحة."), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveDataImage(String dataUri) {
        try {
            int comma = dataUri.indexOf(',');
            if (!dataUri.startsWith("data:") || comma < 0) throw new IllegalStateException("Unsupported image data");
            String header = dataUri.substring(5, comma);
            String contentType = header.split(";", 2)[0];
            String payload = dataUri.substring(comma + 1);
            byte[] bytes = header.contains(";base64") ? Base64.decode(payload, Base64.DEFAULT) : java.net.URLDecoder.decode(payload, "UTF-8").getBytes(java.nio.charset.StandardCharsets.UTF_8);
            saveImageBytes(bytes, "clipnest-image", contentType, selectedFolderUri);
            main.post(() -> Toast.makeText(this, tr("Image saved to the video destination.", "تم حفظ الصورة في مسار الفيديو."), Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            main.post(() -> Toast.makeText(this, tr("Could not download image: ", "تعذر تنزيل الصورة: ") + safeMessage(e), Toast.LENGTH_LONG).show());
        }
    }

    private Uri saveImageBytes(byte[] bytes, String filenameHint, String contentType, Uri targetFolder) throws Exception {
        String mime = imageMime(contentType, filenameHint);
        String extension = imageExtension(mime);
        String base = filenameHint == null ? "clipnest-image" : filenameHint.replaceFirst("\\.[^.]+$", "");
        base = base.replaceAll("[^A-Za-z0-9._-]", "_");
        File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        String rawExtension = "image/converted".equals(mime) ? ".clipnest-source" : extension;
        File raw = new File(dir == null ? getFilesDir() : dir, base + rawExtension);
        try (OutputStream output = new FileOutputStream(raw)) {
            output.write(bytes);
            output.flush();
        }
        File finalFile = raw;
        if (!"image/gif".equals(mime) && !"image/png".equals(mime) && !"image/jpeg".equals(mime)) {
            Bitmap bitmap = BitmapFactory.decodeFile(raw.getAbsolutePath());
            if (bitmap == null) throw new IllegalStateException("Unsupported image data");
            finalFile = new File(raw.getParentFile(), base + ".jpg");
            try (OutputStream output = new FileOutputStream(finalFile)) {
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)) throw new IllegalStateException("Image conversion failed");
            }
            bitmap.recycle();
            raw.delete();
            mime = "image/jpeg";
        }
        Uri saved = saveImageToPreferredDestination(finalFile, mime, targetFolder);
        finalFile.delete();
        return saved;
    }

    private Uri saveImageToPreferredDestination(File source, String mime, Uri targetFolder) throws Exception {
        if (targetFolder == null) return copyToDownloads(source, mime);
        try {
            return copyToSelectedFolder(source, mime, targetFolder);
        } catch (SecurityException | IllegalStateException e) {
            return copyToDownloads(source, mime);
        }
    }

    private Uri saveToPreferredDestination(File source, String mime, Uri targetFolder) throws Exception {
        if (targetFolder == null) return copyToDownloads(source, mime);
        try {
            return copyToSelectedFolder(source, mime, targetFolder);
        } catch (SecurityException | IllegalStateException e) {
            return copyToDownloads(source, mime);
        }
    }

    private String imageMime(String contentType, String filename) {
        String type = contentType == null ? "" : contentType.toLowerCase(java.util.Locale.ROOT);
        if (type.contains("image/gif") || endsWith(filename, ".gif")) return "image/gif";
        if (type.contains("image/png") || endsWith(filename, ".png")) return "image/png";
        if (type.contains("image/jpeg") || type.contains("image/jpg") || endsWith(filename, ".jpg") || endsWith(filename, ".jpeg")) return "image/jpeg";
        return "image/converted";
    }

    private String imageExtension(String mime) {
        if ("image/gif".equals(mime)) return ".gif";
        if ("image/png".equals(mime)) return ".png";
        return ".jpg";
    }

    private boolean endsWith(String value, String suffix) {
        return value != null && value.toLowerCase(java.util.Locale.ROOT).endsWith(suffix);
    }

    private void copyToClipboard(String label, String value) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) clipboard.setPrimaryClip(ClipData.newPlainText(label, value));
    }

    private void loadBrowserAddress() {
        String value = browserAddressInput.getText().toString().trim();
        if (value.isEmpty()) return;
        Uri uri = Uri.parse(value);
        if (!isHttpUrl(uri)) {
            String searchUrl = "https://duckduckgo.com/?q=" + Uri.encode(value);
            browserWebView.loadUrl(searchUrl);
            return;
        }
        browserWebView.loadUrl(uri.toString());
    }

    private boolean isHttpUrl(Uri uri) {
        String scheme = uri.getScheme();
        return uri.getHost() != null && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
    }

    private boolean isSignInUrl(Uri uri) {
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(java.util.Locale.ROOT);
        String path = uri.getPath() == null ? "" : uri.getPath().toLowerCase(java.util.Locale.ROOT);
        return host.equals("accounts.google.com") || host.equals("accounts.youtube.com")
                || (host.endsWith("youtube.com") && (path.contains("signin") || path.contains("login")));
    }

    private void startYoutubeSignIn() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putBoolean(PREF_WEB_COOKIES, true)
                .putBoolean(PREF_WEB_THIRD_PARTY_COOKIES, true)
                .putBoolean(PREF_WEB_DOM_STORAGE, true)
                .apply();
        applyWebBrowserSettings();
        showPage(2);
        browserAddressInput.setText("https://accounts.google.com/ServiceLogin?continue=https://www.youtube.com");
        browserStatusText.setText("Sign in to your own Google/YouTube account, then use Export cookies.");
        browserWebView.loadUrl(browserAddressInput.getText().toString());
    }

    private void restoreImportedCookieFile() {
        SharedPreferences preferences = getSharedPreferences(PREFS, MODE_PRIVATE);
        String saved = preferences.getString(PREF_IMPORTED_COOKIE_FILE, null);
        importedCookieUserAgent = preferences.getString(PREF_COOKIE_USER_AGENT, null);
        if (saved != null) {
            File candidate = new File(saved);
            if (candidate.isFile() && candidate.length() > 0) importedCookieFile = candidate;
        }
        refreshCookieStatus();
    }

    private void chooseCookieFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, COOKIE_IMPORT_REQUEST);
    }

    private void importCookieDocument(Uri source) {
        File target = new File(getFilesDir(), "youtube-cookies.txt");
        try (InputStream input = getContentResolver().openInputStream(source);
             OutputStream output = new java.io.FileOutputStream(target)) {
            if (input == null) throw new IllegalStateException("Could not open the selected cookie file.");
            byte[] buffer = new byte[8192];
            int count;
            long total = 0;
            while ((count = input.read(buffer)) != -1) {
                total += count;
                if (total > 5_000_000) throw new IllegalArgumentException("Cookie file is unexpectedly large.");
                output.write(buffer, 0, count);
            }
            if (target.length() == 0) throw new IllegalArgumentException("Cookie file is empty.");
            importedCookieFile = target;
            String currentUserAgent = browserWebView == null ? null : browserWebView.getSettings().getUserAgentString();
            SharedPreferences.Editor editor = getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString(PREF_IMPORTED_COOKIE_FILE, target.getAbsolutePath());
            if (currentUserAgent != null && !currentUserAgent.trim().isEmpty()) {
                importedCookieUserAgent = currentUserAgent;
                editor.putString(PREF_COOKIE_USER_AGENT, currentUserAgent);
            }
            editor.apply();
            refreshCookieStatus();
            setStatus("cookies.txt imported. Downloads will use this local file.");
        } catch (Exception e) {
            Toast.makeText(this, "Could not import cookies.txt: " + safeMessage(e), Toast.LENGTH_LONG).show();
        }
    }

    private void exportWebViewCookies() {
        if (browserWebView == null) {
            Toast.makeText(this, "Open the Browser tab and sign in first.", Toast.LENGTH_LONG).show();
            return;
        }
        CookieManager.getInstance().flush();
        String cookieText = collectWebViewCookies();
        if (cookieText.trim().equals("# Netscape HTTP Cookie File\n# Exported locally by ClipNest. Treat this file like a password.")) {
            Toast.makeText(this, "No WebView cookies found. Sign in to YouTube first.", Toast.LENGTH_LONG).show();
            return;
        }
        pendingCookieExportText = cookieText;
        String userAgent = browserWebView.getSettings().getUserAgentString();
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_COOKIE_USER_AGENT, userAgent).apply();
        importedCookieUserAgent = userAgent;
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "youtube-cookies.txt");
        startActivityForResult(intent, COOKIE_EXPORT_REQUEST);
    }

    private String buildNetscapeCookieText(String youtube, String google) {
        StringBuilder output = new StringBuilder("# Netscape HTTP Cookie File\n# Exported locally by ClipNest. Treat this file like a password.\n");
        appendCookieLines(output, ".youtube.com", youtube);
        appendCookieLines(output, ".google.com", google);
        return output.toString();
    }

    private String collectWebViewCookies() {
        StringBuilder output = new StringBuilder("# Netscape HTTP Cookie File\n# Exported locally by ClipNest. Treat this file like a password.\n");
        appendCookieLines(output, ".youtube.com", CookieManager.getInstance().getCookie("https://www.youtube.com"));
        appendCookieLines(output, ".youtube.com", CookieManager.getInstance().getCookie("https://youtube.com"));
        appendCookieLines(output, ".youtube.com", CookieManager.getInstance().getCookie("https://m.youtube.com"));
        appendCookieLines(output, ".google.com", CookieManager.getInstance().getCookie("https://accounts.google.com"));
        appendCookieLines(output, ".google.com", CookieManager.getInstance().getCookie("https://google.com"));
        return output.toString();
    }

    private void appendCookieLines(StringBuilder output, String domain, String cookieHeader) {
        if (cookieHeader == null) return;
        for (String item : cookieHeader.split(";")) {
            String[] pair = item.trim().split("=", 2);
            if (pair.length == 2 && !pair[0].trim().isEmpty()) {
                output.append(domain).append("\tTRUE\t/\tTRUE\t2147483647\t")
                        .append(pair[0].trim()).append("\t").append(pair[1].trim()).append("\n");
            }
        }
    }

    private void exportCookieDocument(Uri destination) {
        String cookieText = pendingCookieExportText;
        pendingCookieExportText = null;
        if (cookieText == null) cookieText = buildNetscapeCookieText(
                CookieManager.getInstance().getCookie("https://www.youtube.com"),
                CookieManager.getInstance().getCookie("https://accounts.google.com"));
        try (OutputStream output = getContentResolver().openOutputStream(destination)) {
            if (output == null) throw new IllegalStateException("Could not create the destination file.");
            output.write(cookieText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            Toast.makeText(this, "cookies.txt exported. Keep it private.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Could not export cookies.txt: " + safeMessage(e), Toast.LENGTH_LONG).show();
        }
    }

    private void refreshCookieStatus() {
        if (cookieStatusText == null) return;
        cookieStatusText.setText(importedCookieFile == null
                ? "No yt-dlp cookie file selected. Sign in above or import cookies.txt."
                : "yt-dlp cookie file ready: " + importedCookieFile.getName());
    }

    private void refreshInstalledVersion() {
        if (installedVersionText == null) return;
        try {
            String version = YoutubeDL.getInstance().versionName(getApplicationContext());
            installedVersionText.setText(version == null || version.trim().isEmpty() ? "Bundled yt-dlp build" : version);
        } catch (Exception e) {
            installedVersionText.setText("Version information unavailable");
        }
    }

    private void refreshGalleryVersion() {
        if (galleryVersionText == null) return;
        worker.execute(() -> {
            String version;
            try { version = getPythonRuntime().getModule("gallery_runner").callAttr("installed_version").toString(); }
            catch (Exception e) { version = "Version information unavailable"; }
            String finalVersion = version;
            main.post(() -> galleryVersionText.setText(tr("Installed version: ", "الإصدار المثبّت: ") + finalVersion));
        });
    }

    private void checkForGalleryUpdate() {
        galleryUpdateButton.setEnabled(false);
        galleryUpdateStatusText.setText(tr("Checking gallery-dl.org for the latest release…", "جارٍ التحقق من أحدث إصدار من gallery-dl.org…"));
        worker.execute(() -> {
            String result;
            try { result = getPythonRuntime().getModule("gallery_runner").callAttr("check_update").toString(); }
            catch (Exception e) { result = "Gallery-dl update check failed: " + safeMessage(e); }
            String finalResult = result;
            main.post(() -> { galleryUpdateButton.setEnabled(true); galleryUpdateStatusText.setText(finalResult); refreshGalleryVersion(); });
        });
    }

    private LinearLayout qualityRow(LinearLayout parent, String labelText, boolean video) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rowParams = params(-1, -2);
        rowParams.topMargin = dp(6);
        parent.addView(row, rowParams);
        row.addView(label(labelText));
        TextView selector = new TextView(this);
        selector.setTextColor(TEXT);
        selector.setTextSize(15);
        selector.setGravity(Gravity.CENTER_VERTICAL);
        selector.setPadding(dp(14), 0, dp(14), 0);
        selector.setBackground(rounded(Color.rgb(12, 15, 20), 12));
        selector.setClipToOutline(true);
        LinearLayout.LayoutParams selectorParams = params(-1, dp(50));
        selectorParams.topMargin = dp(7);
        row.addView(selector, selectorParams);
        selector.setOnClickListener(v -> showQualityMenu(video));
        row.setTag(selector);
        return row;
    }

    private RadioButton galleryOutputButton(String text, String mode) {
        RadioButton button = new RadioButton(this);
        button.setId(View.generateViewId());
        button.setText(text);
        button.setTag(mode);
        button.setTextColor(TEXT);
        button.setTextSize(13);
        button.setButtonTintList(ColorStateList.valueOf(ACCENT));
        LinearLayout.LayoutParams buttonParams = params(0, -1);
        buttonParams.weight = 1f;
        button.setLayoutParams(buttonParams);
        return button;
    }

    private String localizedVideoQuality(int index) {
        if (!isArabic()) return VIDEO_QUALITY_LABELS[index];
        String[] labels = {"الأفضل المتاح", "حتى 2160p", "حتى 1440p", "حتى 1080p", "حتى 720p", "حتى 480p", "حتى 360p"};
        return labels[index];
    }

    private String localizedAudioQuality(int index) {
        if (!isArabic()) return AUDIO_QUALITY_LABELS[index];
        String[] labels = {"أفضل MP3 · V0", "جودة عالية MP3 · V2", "جودة قياسية MP3 · V5", "MP3 · 128 kbps"};
        return labels[index];
    }

    private String[] localizedVideoQualityOptions() {
        String[] options = new String[VIDEO_QUALITY_LABELS.length];
        for (int i = 0; i < options.length; i++) options[i] = localizedVideoQuality(i);
        return options;
    }

    private String[] localizedAudioQualityOptions() {
        String[] options = new String[AUDIO_QUALITY_LABELS.length];
        for (int i = 0; i < options.length; i++) options[i] = localizedAudioQuality(i);
        return options;
    }

    private void updateQualityLabels() {
        if (videoQualityChoice != null) videoQualityChoice.setText(localizedVideoQuality(videoQualityIndex) + "    ⌄");
        if (audioQualityChoice != null) audioQualityChoice.setText(localizedAudioQuality(audioQualityIndex) + "    ⌄");
    }

    private void showQualityMenu(boolean video) {
        String[] options = video ? localizedVideoQualityOptions() : localizedAudioQualityOptions();
        int selected = video ? videoQualityIndex : audioQualityIndex;
        Dialog dialog = new Dialog(this);
        LinearLayout card = dialogCard();

        TextView title = new TextView(this);
        title.setText(video ? tr("Video quality", "جودة الفيديو") : tr("Audio quality", "جودة الصوت"));
        title.setTextColor(TEXT);
        title.setTextSize(21);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText(video ? tr("Choose the maximum resolution to request.", "اختر أقصى دقة مطلوبة.") : tr("Choose the MP3 encoding quality.", "اختر جودة ترميز MP3.") );
        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(13);
        LinearLayout.LayoutParams subtitleParams = params(-1, -2);
        subtitleParams.topMargin = dp(7);
        card.addView(subtitle, subtitleParams);

        ScrollView choicesScroll = new ScrollView(this);
        LinearLayout choices = new LinearLayout(this);
        choices.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams choicesParams = params(-1, -2);
        choicesParams.topMargin = dp(17);
        card.addView(choicesScroll, choicesParams);
        choicesScroll.addView(choices);
        for (int i = 0; i < options.length; i++) {
            final int index = i;
            boolean isSelected = i == selected;
            LinearLayout item = new LinearLayout(this);
            item.setGravity(Gravity.CENTER_VERTICAL);
            item.setPadding(dp(15), dp(11), dp(15), dp(11));
            item.setBackground(rounded(isSelected ? Color.rgb(26, 54, 56) : Color.rgb(12, 15, 20), 14));
            item.setClipToOutline(true);
            if (isSelected) ((GradientDrawable) item.getBackground()).setStroke(dp(1), ACCENT);
            LinearLayout.LayoutParams itemParams = params(-1, dp(52));
            itemParams.bottomMargin = dp(8);
            choices.addView(item, itemParams);
            TextView option = new TextView(this);
            option.setText(options[i]);
            option.setTextColor(isSelected ? ACCENT : TEXT);
            option.setTextSize(15);
            option.setTypeface(Typeface.DEFAULT, isSelected ? Typeface.BOLD : Typeface.NORMAL);
            item.addView(option, new LinearLayout.LayoutParams(0, -2, 1f));
            if (isSelected) {
                TextView check = new TextView(this);
                check.setText("✓");
                check.setTextColor(ACCENT);
                check.setTextSize(18);
                check.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                item.addView(check);
            }
            item.setOnClickListener(v -> {
                if (video) videoQualityIndex = index;
                else audioQualityIndex = index;
                updateQualityLabels();
                persistQualityChoices();
                dialog.dismiss();
            });
        }
        configureCardDialog(dialog, card, true);
    }

    private void showSettingsPage() {
        Dialog dialog = new Dialog(this);
        LinearLayout card = dialogCard();

        TextView title = new TextView(this);
        title.setText("Settings & updates");
        title.setTextColor(TEXT);
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(title);

        TextView description = new TextView(this);
        description.setText("ClipNest checks yt-dlp's nightly channel on startup. yt-dlp recommends nightly for regular use. If an extractor reports an over-90-days version, the app updates and retries once.");
        description.setTextColor(MUTED);
        description.setTextSize(14);
        description.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams descriptionParams = params(-1, -2);
        descriptionParams.topMargin = dp(10);
        card.addView(description, descriptionParams);

        TextView updateState = new TextView(this);
        updateState.setText(updateStatusSummary);
        updateState.setTextColor(ACCENT);
        updateState.setTextSize(13);
        LinearLayout.LayoutParams updateStateParams = params(-1, -2);
        updateStateParams.topMargin = dp(15);
        card.addView(updateState, updateStateParams);

        Button updateButton = actionButton("Check & update nightly", Color.rgb(26, 54, 56), ACCENT, ACCENT, 14);
        LinearLayout.LayoutParams updateButtonParams = params(-1, dp(48));
        updateButtonParams.topMargin = dp(12);
        card.addView(updateButton, updateButtonParams);
        updateButton.setOnClickListener(v -> checkForYtDlpUpdate(updateState, updateButton));

        TextView privacy = new TextView(this);
        privacy.setText("Privacy: ClipNest does not read, export, or save sign-in cookies. It cannot pass a logged-in session to yt-dlp or bypass age restrictions and other access controls.");
        privacy.setTextColor(MUTED);
        privacy.setTextSize(13);
        privacy.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams privacyParams = params(-1, -2);
        privacyParams.topMargin = dp(17);
        card.addView(privacy, privacyParams);

        Button close = actionButton("Done", Color.rgb(38, 44, 52), TEXT, Color.rgb(58, 66, 76), 14);
        LinearLayout.LayoutParams closeParams = params(-1, dp(46));
        closeParams.topMargin = dp(18);
        card.addView(close, closeParams);
        close.setOnClickListener(v -> dialog.dismiss());

        configureCardDialog(dialog, card, false);
    }

    private LinearLayout dialogCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(22), dp(20), dp(18));
        GradientDrawable background = rounded(PANEL, 24);
        background.setStroke(dp(1), Color.rgb(44, 53, 63));
        card.setBackground(background);
        card.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        card.setClipToOutline(true);
        card.setElevation(dp(10));
        return card;
    }

    private void configureCardDialog(Dialog dialog, View content, boolean bottomSheet) {
        dialog.setContentView(content);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.dimAmount = 0.62f;
            window.setAttributes(attributes);
        }
        dialog.show();
        if (window != null) {
            window.setGravity(bottomSheet ? Gravity.BOTTOM : Gravity.CENTER);
            window.setLayout(getResources().getDisplayMetrics().widthPixels - dp(30), WindowManager.LayoutParams.WRAP_CONTENT);
            if (bottomSheet) {
                WindowManager.LayoutParams attributes = window.getAttributes();
                attributes.y = dp(12);
                window.setAttributes(attributes);
            }
        }
    }

    private void checkForYtDlpUpdate(TextView state, Button button) {
        if (!engineReady) {
            state.setText("The download engine is still starting.");
            return;
        }
        if (updateInProgress) {
            state.setText("A yt-dlp update check is already in progress.");
            return;
        }
        updateInProgress = true;
        state.setText("Checking the recommended nightly channel…");
        setUpdateButtonsEnabled(false);
        worker.execute(() -> {
            try {
                String result = updateRecommendedYtDlp();
                updateStatusSummary = result;
                main.post(() -> {
                    state.setText(result);
                });
            } catch (Exception e) {
                String message = "Update failed: " + safeMessage(e);
                updateStatusSummary = message;
                main.post(() -> {
                    state.setText(message);
                });
            } finally {
                updateInProgress = false;
                main.post(() -> {
                    setUpdateButtonsEnabled(true);
                    refreshInstalledVersion();
                    if (updatePageStatusText != null) updatePageStatusText.setText(updateStatusSummary);
                });
            }
        });
    }

    private void setUpdateButtonsEnabled(boolean enabled) {
        if (updateYtDlpButton != null) updateYtDlpButton.setEnabled(enabled);
        if (updatePageButton != null) updatePageButton.setEnabled(enabled);
    }

    private String updateRecommendedYtDlp() throws YoutubeDLException {
        YoutubeDL.UpdateStatus result = YoutubeDL.getInstance().updateYoutubeDL(
                getApplicationContext(), YoutubeDL.UpdateChannel._NIGHTLY);
        return result == YoutubeDL.UpdateStatus.DONE
                ? "yt-dlp updated to the latest nightly release."
                : "yt-dlp nightly is already up to date.";
    }

    private Button actionButton(String text, int background, int textColor, int strokeColor, int textSize) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(textColor);
        button.setTextSize(textSize);
        button.setAllCaps(false);
        button.setStateListAnimator(null);
        button.setElevation(0);
        button.setPadding(dp(15), 0, dp(15), 0);
        GradientDrawable shape = rounded(background, 13);
        if (strokeColor != background) shape.setStroke(dp(1), strokeColor);
        boolean animations = getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(PREF_ANIMATIONS, true);
        button.setBackground(animations
                ? new RippleDrawable(ColorStateList.valueOf(Color.argb(72, 255, 255, 255)), shape, null)
                : shape);
        return button;
    }

    private void restorePreferences() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        videoQualityIndex = Math.max(0, Math.min(VIDEO_QUALITY_LABELS.length - 1, prefs.getInt(PREF_VIDEO_QUALITY, 0)));
        audioQualityIndex = Math.max(0, Math.min(AUDIO_QUALITY_LABELS.length - 1, prefs.getInt(PREF_AUDIO_QUALITY, 0)));
        updateQualityLabels();
        String saved = prefs.getString(PREF_FOLDER_URI, null);
        if (saved != null) {
            Uri candidate = Uri.parse(saved);
            boolean permissionFound = false;
            for (android.content.UriPermission permission : getContentResolver().getPersistedUriPermissions()) {
                if (candidate.equals(permission.getUri()) && permission.isWritePermission()) {
                    permissionFound = true;
                    break;
                }
            }
            if (permissionFound) {
                selectedFolderUri = candidate;
                refreshDestinationLabel();
            } else {
                prefs.edit().remove(PREF_FOLDER_URI).apply();
                refreshDestinationLabel();
            }
        } else {
            refreshDestinationLabel();
        }
        String gallerySaved = prefs.getString(PREF_GALLERY_FOLDER_URI, null);
        if (gallerySaved != null) {
            Uri candidate = Uri.parse(gallerySaved);
            for (android.content.UriPermission permission : getContentResolver().getPersistedUriPermissions()) {
                if (candidate.equals(permission.getUri()) && permission.isWritePermission()) {
                    selectedGalleryFolderUri = candidate;
                    break;
                }
            }
            if (selectedGalleryFolderUri == null) prefs.edit().remove(PREF_GALLERY_FOLDER_URI).apply();
        }
        refreshGalleryDestinationLabel();
    }

    private void chooseFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        if (selectedFolderUri != null && android.os.Build.VERSION.SDK_INT >= 26) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, selectedFolderUri);
        }
        startActivityForResult(intent, FOLDER_PICKER_REQUEST);
    }

    private void chooseGalleryFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        if (selectedGalleryFolderUri != null && android.os.Build.VERSION.SDK_INT >= 26) intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, selectedGalleryFolderUri);
        startActivityForResult(intent, GALLERY_FOLDER_PICKER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COOKIE_IMPORT_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            importCookieDocument(data.getData());
            return;
        }
        if (requestCode == COOKIE_EXPORT_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            exportCookieDocument(data.getData());
            return;
        }
        if (requestCode == GALLERY_FOLDER_PICKER_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try {
                getContentResolver().takePersistableUriPermission(uri, flags);
                selectedGalleryFolderUri = uri;
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_GALLERY_FOLDER_URI, uri.toString()).apply();
                refreshGalleryDestinationLabel();
            } catch (SecurityException e) {
                Toast.makeText(this, "Android could not keep access to that folder. Choose another folder.", Toast.LENGTH_LONG).show();
            }
            return;
        }
        if (requestCode != FOLDER_PICKER_REQUEST || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        try {
            getContentResolver().takePersistableUriPermission(uri, flags);
            selectedFolderUri = uri;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_FOLDER_URI, uri.toString()).apply();
            refreshDestinationLabel();
            if (selectedGalleryFolderUri == null) refreshGalleryDestinationLabel();
            setStatus("Save folder selected. It will be remembered next time.");
        } catch (SecurityException e) {
            Toast.makeText(this, "Android could not keep access to that folder. Choose another folder.", Toast.LENGTH_LONG).show();
        }
    }

    private void refreshDestinationLabel() {
        if (destinationText == null) return;
        if (selectedFolderUri == null) {
            destinationText.setText(tr("Save folder • ClipNest", "مجلد الحفظ • ClipNest"));
            return;
        }
        DocumentFile folder = DocumentFile.fromTreeUri(this, selectedFolderUri);
        String name = folder == null ? null : folder.getName();
        destinationText.setText(tr("Save folder • ", "مجلد الحفظ • ") + (name == null ? tr("Selected location", "الموقع المحدد") : name));
    }

    private void refreshGalleryDestinationLabel() {
        if (galleryDestinationText == null) return;
        Uri uri = selectedGalleryFolderUri != null ? selectedGalleryFolderUri : selectedFolderUri;
        if (uri == null) {
            galleryDestinationText.setText(tr("Save folder • ClipNest", "مجلد الحفظ • ClipNest"));
            return;
        }
        galleryDestinationText.setText(tr("Save folder • ", "مجلد الحفظ • ") + destinationName(uri));
    }

    private Uri galleryTargetFolder() {
        return selectedGalleryFolderUri != null ? selectedGalleryFolderUri : selectedFolderUri;
    }

    private void startDownload() {
        String url = urlInput.getText().toString().trim();
        Uri parsed = Uri.parse(url);
        String scheme = parsed.getScheme();
        if (url.isEmpty() || parsed.getHost() == null || !("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))) {
            urlInput.setError("Enter a full http or https URL");
            return;
        }
        if (!engineReady) {
            setStatus("The download engine is still starting.");
            return;
        }
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(urlInput.getWindowToken(), 0);
        urlInput.clearFocus();
        persistQualityChoices();
        cancelRequested = false;
        processId = UUID.randomUUID().toString();
        lastSavedUri = null;
        openButton.setVisibility(View.GONE);
        downloadButton.setEnabled(false);
        cancelButton.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);
        downloadInProgress = true;
        if (getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(PREF_KEEP_SCREEN_ON, true)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        startDownloadForegroundService();
        updateDownloadNotification(0, "Connecting…");
        setStatus("Connecting…");
        final boolean audioOnly = !videoChoice.isChecked();
        final int videoQuality = videoQualityIndex;
        final int audioQuality = audioQualityIndex;
        final Uri targetFolder = selectedFolderUri;

        worker.execute(() -> {
            File external = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File outputDir = new File(external == null ? getFilesDir() : external, "clipnest-work");
            if (!outputDir.exists() && !outputDir.mkdirs()) {
                finishWithError("Could not create the temporary download folder.");
                return;
            }
            long startedAt = System.currentTimeMillis();
            try {
                YoutubeDLRequest request = new YoutubeDLRequest(url);
                request.addOption("--no-playlist");
                request.addOption("--no-mtime");
                request.addOption("-o", new File(outputDir, "%(title).180B [%(id)s].%(ext)s").getAbsolutePath());
                if (importedCookieFile != null && importedCookieFile.isFile()) {
                    request.addOption("--cookies", importedCookieFile.getAbsolutePath());
                    String cookieUserAgent = importedCookieUserAgent;
                    if ((cookieUserAgent == null || cookieUserAgent.trim().isEmpty()) && browserWebView != null) {
                        cookieUserAgent = browserWebView.getSettings().getUserAgentString();
                    }
                    if (cookieUserAgent != null && !cookieUserAgent.trim().isEmpty()) {
                        request.addOption("--user-agent", cookieUserAgent);
                    }
                }
                if (audioOnly) {
                    request.addOption("-x");
                    request.addOption("--audio-format", "mp3");
                    request.addOption("--audio-quality", AUDIO_QUALITY_ARGS[audioQuality]);
                } else {
                    request.addOption("-f", videoFormatSelector(videoQuality));
                    request.addOption("--merge-output-format", "mp4");
                }
                try {
                    executeRequest(request);
                } catch (Exception firstFailure) {
                    if (cancelRequested || !isOutdatedYtDlpError(firstFailure)) throw firstFailure;
                    main.post(() -> setStatus("yt-dlp is out of date. Updating nightly and retrying…"));
                    updateStatusSummary = updateRecommendedYtDlp();
                    executeRequest(request);
                }
                if (cancelRequested) {
                    finishCancelled();
                    return;
                }
                File output = findOutput(outputDir, startedAt);
                if (output == null) {
                    finishWithError("Download finished, but no output file was found.");
                    return;
                }
                String mime = audioOnly ? "audio/mpeg" : guessMime(output.getName());
                Uri saved = targetFolder == null ? copyToDownloads(output, mime) : copyToSelectedFolder(output, mime, targetFolder);
                output.delete();
                lastSavedUri = saved;
                lastMimeType = mime;
                String destination = targetFolder == null ? "Downloads/ClipNest" : destinationName(targetFolder);
                main.post(() -> {
                    finishDownloadProgress();
                    progressBar.setProgress(100);
                    progressBar.setVisibility(View.GONE);
                    cancelButton.setVisibility(View.GONE);
                    downloadButton.setEnabled(true);
                    openButton.setVisibility(View.VISIBLE);
                    setStatus("Saved to " + destination + "\n" + output.getName());
                });
            } catch (Exception e) {
                if (cancelRequested) finishCancelled();
                else finishWithError("Download failed: " + safeMessage(e));
            }
        });
    }

    private void startGalleryDownload() {
        String url = galleryUrlInput.getText().toString().trim();
        Uri parsed = Uri.parse(url);
        if (url.isEmpty() || !isHttpUrl(parsed)) {
            galleryUrlInput.setError(tr("Enter a full http or https URL", "أدخل رابط http أو https كاملًا"));
            return;
        }
        final Uri targetFolder = galleryTargetFolder();
        final String outputMode = galleryOutputModeValue();
        galleryDownloadButton.setEnabled(false);
        galleryStatusText.setText(tr("Starting gallery-dl…", "جارٍ تشغيل Gallery-dl…"));
        worker.execute(() -> {
            File workspace = null;
            try {
                File external = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                File root = new File(external == null ? getFilesDir() : external, "clipnest-gallery");
                workspace = new File(root, String.valueOf(System.currentTimeMillis()));
                if (!workspace.mkdirs()) throw new IllegalStateException("Could not create Gallery-dl workspace.");
                Python python = getPythonRuntime();
                PyObject runner = python.getModule("gallery_runner");
                String result = runner.callAttr("download_gallery", url, workspace.getAbsolutePath()).toString();
                List<File> files = new ArrayList<>();
                collectFiles(workspace, files);
                if (files.isEmpty()) throw new IllegalStateException("Gallery-dl completed but no files were found.");
                int savedCount = 0;
                for (File file : files) if (file.isFile() && file.length() > 0) savedCount++;
                if (savedCount == 0) throw new IllegalStateException("Gallery-dl produced no usable files.");
                int count = savedCount;
                if (!"folder".equals(outputMode)) {
                    String extension = "cbz".equals(outputMode) ? ".cbz" : ".zip";
                    File archive = new File(workspace, "gallery-download-" + System.currentTimeMillis() + extension);
                    createGalleryArchive(workspace, files, archive, "cbz".equals(outputMode));
                    saveToPreferredDestination(archive, "application/zip", targetFolder);
                } else {
                    for (File file : files) {
                        if (!file.isFile() || file.length() == 0) continue;
                        saveToPreferredDestination(file, guessMime(file.getName()), targetFolder);
                    }
                }
                main.post(() -> {
                    galleryDownloadButton.setEnabled(true);
                    String suffix = "folder".equals(outputMode) ? tr(" in the selected folder.", " في المجلد المحدد.") : tr(" as " + outputMode.toUpperCase(java.util.Locale.ROOT) + ".", " بصيغة " + outputMode.toUpperCase(java.util.Locale.ROOT) + ".");
                    galleryStatusText.setText(tr("Saved " + count + " file(s) with Gallery-dl" + suffix, "تم حفظ " + count + " ملف باستخدام Gallery-dl" + suffix));
                });
            } catch (Exception e) {
                String message = safeMessage(e);
                main.post(() -> {
                    galleryDownloadButton.setEnabled(true);
                    galleryStatusText.setText(tr("Gallery-dl failed: ", "فشل Gallery-dl: ") + message);
                });
            } finally {
                if (workspace != null) deleteRecursively(workspace);
            }
        });
    }

    private String galleryOutputModeValue() {
        if (galleryOutputMode == null || galleryOutputMode.getCheckedRadioButtonId() == -1) return "folder";
        View checked = galleryOutputMode.findViewById(galleryOutputMode.getCheckedRadioButtonId());
        Object tag = checked == null ? null : checked.getTag();
        return tag == null ? "folder" : tag.toString();
    }

    private void createGalleryArchive(File workspace, List<File> files, File archive, boolean cbz) throws Exception {
        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(archive))) {
            for (File file : files) {
                if (!file.isFile() || file.length() == 0) continue;
                String name = file.getName();
                if (cbz && !name.toLowerCase(java.util.Locale.ROOT).matches(".*\\.(jpg|jpeg|png|gif|webp)$")) continue;
                ZipEntry entry = new ZipEntry(name);
                zip.putNextEntry(entry);
                try (InputStream input = new FileInputStream(file)) { copy(input, zip); }
                zip.closeEntry();
            }
        }
        if (archive.length() == 0) throw new IllegalStateException("Gallery-dl produced no files suitable for this archive format.");
    }

    private Python getPythonRuntime() {
        if (!Python.isStarted()) Python.start(new AndroidPlatform(getApplicationContext()));
        clearChaquopyPythonPath();
        return Python.getInstance();
    }

    /**
     * Chaquopy uses a Python 3.11 PYTHONPATH, while youtubedl-android launches
     * its own Python 3.12 process. Do not let the latter inherit 3.11 bytecode.
     */
    private void clearChaquopyPythonPath() {
        try {
            Os.unsetenv("PYTHONPATH");
        } catch (ErrnoException e) {
            throw new IllegalStateException("Could not isolate yt-dlp from Chaquopy's Python path", e);
        }
    }

    private void collectFiles(File directory, List<File> result) {
        File[] children = directory.listFiles();
        if (children == null) return;
        for (File child : children) {
            if (child.isDirectory()) collectFiles(child, result);
            else result.add(child);
        }
    }

    private void deleteRecursively(File file) {
        if (file == null) return;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) for (File child : children) deleteRecursively(child);
        }
        file.delete();
    }

    private void executeRequest(YoutubeDLRequest request) throws Exception {
        clearChaquopyPythonPath();
        YoutubeDL.getInstance().execute(request, processId, (progress, etaInSeconds, outputLine) -> {
            main.post(() -> {
                if (cancelRequested) return;
                int percent = Math.max(0, Math.min(100, Math.round(progress)));
                progressBar.setProgress(percent);
                updateDownloadNotification(percent, "Downloading · " + percent + "%");
                setStatus("Downloading · " + percent + "%" + (etaInSeconds > 0 ? " · about " + etaInSeconds + " sec left" : ""));
            });
            return Unit.INSTANCE;
        });
    }

    private void updateDownloadNotification(int percent, String message) {
        if (notificationManager == null || !downloadInProgress) return;
        Intent openIntent = new Intent(this, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPendingIntent = PendingIntent.getActivity(this, DOWNLOAD_NOTIFICATION_ID + 1, openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Intent cancelIntent = new Intent(this, DownloadForegroundService.class);
        cancelIntent.setAction(DownloadForegroundService.ACTION_CANCEL_DOWNLOAD);
        cancelIntent.putExtra(DownloadForegroundService.EXTRA_PROCESS_ID, processId);
        PendingIntent cancelPendingIntent = PendingIntent.getActivity(this, DOWNLOAD_NOTIFICATION_ID + 2, cancelIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = new Notification.Builder(this, DOWNLOAD_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("ClipNest download")
                .setContentText(message)
                .setContentIntent(openPendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_PROGRESS)
                .setProgress(100, Math.max(0, Math.min(100, percent)), false)
                .addAction(new Notification.Action.Builder(null, "Cancel", cancelPendingIntent).build());
        notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, builder.build());
    }

    private void finishDownloadProgress() {
        downloadInProgress = false;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (notificationManager != null) notificationManager.cancel(DOWNLOAD_NOTIFICATION_ID);
        stopService(new Intent(this, DownloadForegroundService.class));
    }

    private void startDownloadForegroundService() {
        Intent intent = new Intent(this, DownloadForegroundService.class);
        intent.putExtra(DownloadForegroundService.EXTRA_PROCESS_ID, processId);
        if (android.os.Build.VERSION.SDK_INT >= 26) startForegroundService(intent);
        else startService(intent);
    }

    private boolean isOutdatedYtDlpError(Throwable error) {
        for (Throwable current = error; current != null; current = current.getCause()) {
            String message = current.getMessage();
            if (message == null) continue;
            String normalized = message.toLowerCase(java.util.Locale.ROOT);
            if (normalized.contains("older than 90") || normalized.contains("outdated yt-dlp") || normalized.contains("yt-dlp is outdated")) return true;
        }
        return false;
    }

    private String videoFormatSelector(int index) {
        int height = VIDEO_HEIGHT_LIMITS[index];
        if (height == 0) return "bestvideo*+bestaudio/best";
        return "bestvideo[height<=" + height + "]+bestaudio/best[height<=" + height + "]";
    }

    private void persistQualityChoices() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .putInt(PREF_VIDEO_QUALITY, videoQualityIndex)
                .putInt(PREF_AUDIO_QUALITY, audioQualityIndex)
                .apply();
    }

    private File findOutput(File directory, long startedAt) {
        File[] files = directory.listFiles(file -> file.isFile() && !file.getName().startsWith(".") && !file.getName().endsWith(".part") && !file.getName().endsWith(".ytdl"));
        if (files == null) return null;
        File newest = null;
        for (File file : files) {
            if (file.lastModified() >= startedAt - 10_000 && (newest == null || file.lastModified() > newest.lastModified())) newest = file;
        }
        return newest;
    }

    private Uri copyToDownloads(File source, String mime) throws Exception {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, source.getName());
        values.put(MediaStore.MediaColumns.MIME_TYPE, mime);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ClipNest");
        values.put(MediaStore.MediaColumns.IS_PENDING, 1);
        Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        Uri uri = getContentResolver().insert(collection, values);
        if (uri == null) throw new IllegalStateException("Android could not create a Downloads entry.");
        try (InputStream input = new FileInputStream(source); OutputStream output = getContentResolver().openOutputStream(uri, "w")) {
            if (output == null) throw new IllegalStateException("Could not open the Downloads file.");
            copy(input, output);
        } catch (Exception e) {
            getContentResolver().delete(uri, null, null);
            throw e;
        }
        ContentValues ready = new ContentValues();
        ready.put(MediaStore.MediaColumns.IS_PENDING, 0);
        getContentResolver().update(uri, ready, null, null);
        return uri;
    }

    private Uri copyToSelectedFolder(File source, String mime, Uri treeUri) throws Exception {
        DocumentFile folder = DocumentFile.fromTreeUri(this, treeUri);
        if (folder == null || !folder.canWrite()) throw new IllegalStateException("ClipNest no longer has write access to the selected folder. Choose it again.");
        DocumentFile destination = folder.createFile(mime, source.getName());
        if (destination == null) throw new IllegalStateException("Android could not create the file in the selected folder.");
        try (InputStream input = new FileInputStream(source); OutputStream output = getContentResolver().openOutputStream(destination.getUri(), "w")) {
            if (output == null) throw new IllegalStateException("Could not open the selected-folder output file.");
            copy(input, output);
        } catch (Exception e) {
            destination.delete();
            throw e;
        }
        return destination.getUri();
    }

    private void copy(InputStream input, OutputStream output) throws Exception {
        byte[] buffer = new byte[64 * 1024];
        int count;
        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        output.flush();
    }

    private String destinationName(Uri uri) {
        DocumentFile folder = DocumentFile.fromTreeUri(this, uri);
        String name = folder == null ? null : folder.getName();
        return name == null ? "selected folder" : name;
    }

    private void cancelDownload() {
        cancelRequested = true;
        String id = processId;
        if (id != null) new Thread(() -> YoutubeDL.getInstance().destroyProcessById(id), "clipnest-cancel").start();
        setStatus("Stopping download…");
    }

    private void finishCancelled() {
        main.post(() -> {
            finishDownloadProgress();
            progressBar.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
            downloadButton.setEnabled(true);
            setStatus("Download cancelled.");
        });
    }

    private void finishWithError(String message) {
        main.post(() -> {
            finishDownloadProgress();
            progressBar.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
            downloadButton.setEnabled(true);
            setStatus(message);
        });
    }

    private void openSavedFile() {
        if (lastSavedUri == null) return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(lastSavedUri, lastMimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No app found to open this file. Check your saved folder.", Toast.LENGTH_LONG).show();
        }
    }

    private void showCredits() {
        String credits = "Built with Manus AI\n\n" +
                "AI agents: Manus AI assistant. No additional AI agents were used.\n\n" +
                "Download engine: yt-dlp, embedded through youtubedl-android 0.18.1.\n\n" +
                "Media processing: FFmpeg.\n\n" +
                "Browser: Android WebView with opt-in cookies and user-controlled cookies.txt export/import in Web config.\n\n" +
                "App and build tools: Java, Android SDK, Android Gradle Plugin, and Gradle.\n\n" +
                "Open-source license texts and third-party notices are included in this app and source package.";
        Dialog dialog = new Dialog(this);
        LinearLayout card = dialogCard();
        TextView title = new TextView(this);
        title.setText("About & credits");
        title.setTextColor(TEXT);
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(title);
        TextView detail = new TextView(this);
        detail.setText(credits);
        detail.setTextColor(MUTED);
        detail.setTextSize(14);
        detail.setLineSpacing(dp(3), 1f);
        LinearLayout.LayoutParams detailParams = params(-1, -2);
        detailParams.topMargin = dp(14);
        card.addView(detail, detailParams);
        Button done = actionButton("Done", Color.rgb(38, 44, 52), TEXT, Color.rgb(58, 66, 76), 14);
        LinearLayout.LayoutParams doneParams = params(-1, dp(46));
        doneParams.topMargin = dp(18);
        card.addView(done, doneParams);
        done.setOnClickListener(v -> dialog.dismiss());
        configureCardDialog(dialog, card, false);
    }

    private String guessMime(String filename) {
        int dot = filename.lastIndexOf('.');
        String extension = dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "";
        String type = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return type == null ? "video/mp4" : type;
    }

    private void setStatus(String text) {
        if (statusText != null) {
            statusText.setText(text);
            if (copyErrorButton != null) copyErrorButton.setVisibility(isErrorStatus(text) ? View.VISIBLE : View.GONE);
        }
    }

    private boolean isErrorStatus(String text) {
        String value = text == null ? "" : text.toLowerCase(java.util.Locale.ROOT);
        return value.startsWith("download failed")
                || value.startsWith("could not")
                || value.contains("error")
                || value.contains("failed")
                || value.contains("no output file");
    }

    private void copyCurrentStatus() {
        if (statusText == null) return;
        String message = statusText.getText() == null ? "" : statusText.getText().toString();
        if (message.trim().isEmpty()) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("ClipNest error", message));
            Toast.makeText(this, "Error copied to clipboard.", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyGalleryStatus() {
        if (galleryStatusText == null) return;
        String message = galleryStatusText.getText() == null ? "" : galleryStatusText.getText().toString();
        if (message.trim().isEmpty()) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(ClipData.newPlainText("Gallery-dl error", message));
            Toast.makeText(this, tr("Gallery-dl error copied.", "تم نسخ خطأ Gallery-dl."), Toast.LENGTH_SHORT).show();
        }
    }

    private TextView label(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(MUTED);
        view.setTextSize(11);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setLetterSpacing(0.09f);
        return view;
    }

    private LinearLayout.LayoutParams params(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    private GradientDrawable rounded(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String safeMessage(Throwable t) {
        String message = t.getMessage();
        return message == null || message.trim().isEmpty() ? t.getClass().getSimpleName() : message;
    }

    @Override
    public void onBackPressed() {
        if (navigationPages != null && navigationPages.length > 2 && navigationPages[2].getVisibility() == View.VISIBLE) {
            if (browserWebView != null && browserWebView.canGoBack()) {
                browserWebView.goBack();
                return;
            }
        }
        if (navigationPages != null && navigationPages.length > 0 && navigationPages[0].getVisibility() != View.VISIBLE) {
            showPage(0);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        persistQualityChoices();
        if (browserWebView != null) browserWebView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (browserWebView != null) browserWebView.onResume();
        renderBatteryButton();
        if (batteryStatusText != null) batteryStatusText.setText(isBatteryOptimizationIgnored()
                ? tr("Battery optimization is disabled for ClipNest.", "تم تعطيل تحسين البطارية لتطبيق ClipNest.")
                : tr("Android may pause background work. Tap the button and approve the system request.", "قد يوقف Android العمل في الخلفية. اضغط الزر ووافق على طلب النظام."));
    }

    @Override
    protected void onDestroy() {
        if (processId != null && !downloadInProgress) {
            cancelRequested = true;
            String id = processId;
            new Thread(() -> YoutubeDL.getInstance().destroyProcessById(id), "clipnest-stop-on-close").start();
        }
        if (browserWebView != null) {
            browserWebView.stopLoading();
            browserWebView.clearHistory();
            browserWebView.clearCache(true);
            browserWebView.destroy();
            browserWebView = null;
        }
        if (!downloadInProgress) worker.shutdown();
        super.onDestroy();
    }
}
