# Implementation Plan - CineGen Studio (AI Video & Image Creator)

Build a high-performance, cinematic Android application (**CineGen Studio**) for AI image and video generation, featuring a customizable styles & templates library, client-side MP4 video rendering/export engine using Android MediaCodec/MediaMuxer, prompt refinement with Gemini AI, and compliant Google AdMob integration (Adaptive Banner Ads + Rewarded Ads to unlock HD video exports).

---

## Proposed User Experience & Architecture

### 1. App Identity & Theme
- **App Name**: CineGen Studio (launcher label: `CineGen`, package: `com.aistudio.cinegen.studio`)
- **Theme**: Premium cinematic dark studio aesthetic with vibrant neon/amber accents (`#0D0E15` obsidian canvas, `#6C5CE7` electric violet, `#00CEC9` cyan flair, and `#FD79A8` glow).

### 2. Key Capabilities & Modules
1. **AI Image & Prompt Synthesis Engine**:
   - High-fidelity prompt builder with camera lens presets (35mm Anamorphic, IMAX 70mm, Macro Cyberpunk), lighting controls (Cinematic volumetric, golden hour, neon noir), motion descriptors (Dolly zoom, pan, hyperlapse), and aspect ratios (16:9, 9:16, 1:1, 4:5).
   - Gemini-powered prompt enhancer to expand simple text into rich, photorealistic production prompts.
   - AI Image generator and style transfer renderer with local caching and gallery storage.

2. **Text-to-Video Engine & MP4 Exporter**:
   - Generation of cinematic animated sequence clips (multi-frame motion interpolation, 2.5D parallax camera motion, dynamic lens flares, zoom & pan effects).
   - Real hardware-accelerated MP4 video encoder using Android's `MediaCodec`, `MediaMuxer`, and `Surface` rendering, outputting standard playable `.mp4` video files to app storage and MediaStore.
   - Interactive in-app video player with scrubbing, looping, aspect-ratio switching, and playback speed control.

3. **Custom Styles & Template Library (Room DB)**:
   - Built-in curated styles: *Cyberpunk 2099*, *Vintage 35mm Film*, *Anime Makoto Shinkai*, *Dark Fantasy Unreal Engine 5*, *Sci-Fi Space Odyssey*, *Claymation Stop-Motion*, *Steampunk Brass*.
   - User-defined template builder: Save custom prompts, seed parameters, motion curves, and style tags to Room database. Edit, duplicate, favorite, and export templates.

4. **AdMob Integration (Google Mobile Ads)**:
   - Official Google Mobile Ads SDK (`play-services-ads`).
   - Non-intrusive sticky bottom Adaptive Banner ads on main studio screens.
   - Rewarded Video Ad flow: Users can generate standard preview clips freely, and watch a rewarded ad (or use earned render credits) to unlock 1080p 60fps MP4 export and watermark removal.
   - AdMob Test Ad Unit IDs configured out-of-the-box for safe immediate testing with zero policy violations, plus custom Ad Unit ID configuration in Settings.

5. **Project Studio & Gallery**:
   - Organize created videos and images into projects.
   - One-tap sharing to Instagram, TikTok, YouTube Shorts, or save directly to device gallery via MediaStore.
   - Export history and resolution selector (720p, 1080p, 4K upscale simulation).

---

## Technical Implementation Steps

### Step 1: Dependencies & Android Manifest
- Add Google Mobile Ads SDK (`play-services-ads`), Coil Compose (`coil-compose`), Media3/ExoPlayer for smooth preview playback, and Room database setup.
- Configure `AndroidManifest.xml` with `INTERNET`, `ACCESS_NETWORK_STATE`, AdMob Application ID metadata, and storage/media permissions where needed.
- Update `metadata.json`, `app_name` in `strings.xml`, and configure unique `applicationId`.

### Step 2: Local Database & Architecture (Room + MVVM)
- Define Room Entities: `TemplateEntity`, `GeneratedMediaEntity`, `ProjectEntity`.
- Create DAOs and Repository pattern for offline persistence of generated clips, custom user templates, and credit balances.
- Create ViewModels: `StudioViewModel`, `TemplatesViewModel`, `GalleryViewModel`, and `AdMobManager`.

### Step 3: MP4 Hardware Video Rendering Engine
- Build `VideoEncoder`: Android native `MediaCodec` (AVC/H.264) + `MediaMuxer` pipeline capable of generating clean MP4 files from composed frames, text overlays, and animation shaders.
- Support keyframe transitions, smooth camera zoom/pan (Ken Burns cinematic effect), particle overlays, and color grading LUTs.

### Step 4: AdMob & Rewarded Ad Manager
- Implement `AdMobManager` with standard Google test ad units (`ca-app-pub-3940256099942544/6300978111` for banners and `ca-app-pub-3940256099942544/5224354917` for rewarded ads).
- Compose banner wrapper composable `AdMobBannerView` with lifecycle handling.
- Rewarded ad listener that awards HD video export tokens upon completion.

### Step 5: Compose UI Screens & Navigation
- **Studio Screen**: Prompt editor, AI prompt expander button, camera/motion controls, style chip selector, real-time live preview player, and generation progress indicator.
- **Templates & Styles Library**: Grid view of templates with search, categories, "Create New Template" dialog, and preview cards.
- **Export & Video Player Modal**: Full-screen video player with timeline scrubber, export resolution options, rewarded ad prompt, and direct MP4 download/share.
- **Gallery / Saved Projects Screen**: Categorized grid of all rendered videos and generated stills.
- **Settings Screen**: AdMob configuration, credit counter, Gemini API key status check, resolution settings.

### Step 6: Polish & Adaptive Icon
- Generate custom adaptive app icon and cinematic branding assets.
- Verify smooth compilation and zero warnings via `compile_applet`.

---

## Verification Plan
1. **Compilation Check**: Run `compile_applet` to ensure Gradle builds without errors.
2. **Video & Image Generation Pipeline**: Verify frame rendering and MP4 encoding process without memory leaks.
3. **AdMob Integration**: Verify banner ads display test banners cleanly without layout shift, and rewarded ad callback correctly unlocks MP4 export.
4. **Data Persistence**: Verify templates and creations persist across launches using Room.
