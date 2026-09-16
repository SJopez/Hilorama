<div>
<h1>
<img src="readme/ic_launcher-playstore.png" alt="Hilorama app icon" width="38" height="38" style="border-radius: 12px; vertical-align: text-bottom; display: inline-block; margin-bottom: 0; transform: translateY(-2px);" />
Hilorama
</h1>
</div>

<p align="center">
  <img src="readme/hilorama.gif" alt="Hilorama animation preview" width="360" />
</p>

<p align="center">
  <strong>Girl with a Pearl Earring — Johannes Vermeer</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/C%2B%2B-00599C?style=for-the-badge&logo=c%2B%2B&logoColor=white" alt="C++" />
  <img src="https://img.shields.io/badge/CMake-064F8C?style=for-the-badge&logo=cmake&logoColor=white" alt="CMake" />
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://camo.githubusercontent.com/fd4512ba1584d530fd2c0c8eb81aec73215f180c98609700bb3da3a940c885b6/68747470733a2f2f696d672e736869656c64732e696f2f62616467652f4a65747061636b253230436f6d706f73652d3432383546343f7374796c653d666f722d7468652d6261646765266c6f676f3d6a65747061636b636f6d706f7365266c6f676f436f6c6f723d7768697465" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/NDK-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="NDK" />
</p>

Hilorama is a high-performance String Art generator for Android, combining a native C++ engine with a modern Jetpack Compose interface to transform images into artistic string-based compositions and export them as images or videos.

## Table of Contents

- [Themes and design](#themes-and-design)
- [Drawing](#drawing)
- [Features](#features)
- [Step by step](#step-by-step)
- [Bookmark](#bookmark)
- [Performance](#performance)
- [The end](#the-end)

## Themes and design

From the UI design there is not much to say: the app has light and dark themes according to your preference.

<p align="center">
  <table>
    <tr>
      <td align="center"><img src="readme/light.jpg" alt="Light theme preview" width="260" style="border-radius: 24px; object-fit: cover; box-shadow: 0 10px 20px rgba(0,0,0,0.18);" /><br /><strong>Light</strong></td>
      <td align="center"><img src="readme/dark.jpg" alt="Dark theme preview" width="260" style="border-radius: 24px; object-fit: cover; box-shadow: 0 10px 20px rgba(0,0,0,0.18);" /><br /><strong>Dark</strong></td>
    </tr>
  </table>
</p>

If you are worried about those beautiful purple lines disappearing, don't be. It is just an animation, and it will come back :p

## Drawing

### Coloring

Unlike most String Art apps that generate monochrome compositions, Hilorama can produce colored string-art output in four different modes. Each mode changes the way the drawing interprets the source image and the type of visual mood it creates.

<p align="center">
  <table>
    <tr>
      <td><img src="readme/Gray.jpg" alt="Gray mode example" width="220" style="border-radius: 50%; object-fit: cover; border: 2px solid #e5e7eb;" /></td>
      <td><strong>Gray</strong><br />Uses black and white only. The best choice for a black-and-white style and excellent for preserving fine details.</td>
    </tr>
    <tr>
      <td><img src="readme/Mono.jpg" alt="Mono mode example" width="220" style="border-radius: 50%; object-fit: cover; border: 2px solid #e5e7eb;" /></td>
      <td><strong>Mono</strong><br />Uses white and black in the inverse configuration. It produces an image with strong brightness and works very well for images with light backgrounds.</td>
    </tr>
    <tr>
      <td><img src="readme/RGB.jpg" alt="RGB mode example" width="220" style="border-radius: 50%; object-fit: cover; border: 2px solid #e5e7eb;" /></td>
      <td><strong>RGB</strong><br />Uses Red, Green, and Blue channels, which combine to produce the full range of tones. Like Gray, it produces images with high brightness, and the recommended use case is essentially the same.</td>
    </tr>
    <tr>
      <td><img src="readme/CMY.jpg" alt="CMY mode example" width="220" style="border-radius: 50%; object-fit: cover; border: 2px solid #e5e7eb;" /></td>
      <td><strong>CMY</strong><br />Uses Cyan, Magenta, and Yellow channels. This is the subtractive RGB mode, and it is often the mode you will want to use most of the time because it produces very good results in many cases.</td>
    </tr>
  </table>
</p>

These modes allow the user to choose the visual language of the generated work, from the minimalism of monochrome to the richness of additive or subtractive color systems.

Not every image produces equally good results in every mode, so it is worth trying different color settings to find the combination that fits each composition best.

### Parameters

Parameters are the main controls that shape the final composition in Hilorama. They determine how dense the drawing becomes and how much work the engine needs to perform to produce the final String Art result.

#### Threads

The number of threads directly affects the amount of detail and the complexity of the generated drawing. More threads usually create a richer approximation, but the computational cost also increases.

<p align="center">
  <table>
    <tr>
      <td align="center"><img src="readme/thread0.jpg" alt="Hilorama with 3000 threads" width="220" style="border-radius: 50%; object-fit: cover;" /><br /><strong>3000 threads</strong></td>
      <td align="center"><img src="readme/thread1.jpg" alt="Hilorama with 6000 threads" width="220" style="border-radius: 50%; object-fit: cover;" /><br /><strong>6000 threads</strong></td>
      <td align="center"><img src="readme/thread2.jpg" alt="Hilorama with 9000 threads" width="220" style="border-radius: 50%; object-fit: cover;" /><br /><strong>9000 threads</strong></td>
    </tr>
  </table>
</p>

As you can see here, the right choice is 6000 threads. However, you may choose up to 12000, but in most cases this quantity will be too much. Of course, it depends on the image.

#### Nails

Unlike threads, the maximum number of nails will produce a more detailed image, but like threads, more nails increase significantly the time complexity. However, the recommended approach is to leave the nails at 360 and vary the threads parameter instead. Here you can see an example with a picture of the great Robe Iniesta ❤

<p align="center">
  <table>
    <tr>
      <td align="center"><img src="readme/nails2.jpg" alt="Hilorama with 360 nails" width="220" style="border-radius: 50%; object-fit: cover;" /><br /><strong>360 nails</strong></td>
      <td align="center"><img src="readme/nails1.jpg" alt="Hilorama with 180 nails" width="220" style="border-radius: 50%; object-fit: cover;" /><br /><strong>180 nails</strong></td>
      <td align="center"><img src="readme/nails0.jpg" alt="Hilorama with 150 nails" width="220" style="border-radius: 50%; object-fit: cover;" /><br /><strong>150 nails</strong></td>
    </tr>
  </table>
</p>

## Features

### AI Image

The app can generate a source image through a request to Pollinations AI using a prompt defined by the user. Once the image is ready, it is passed into the app's string-art generation pipeline, where the algorithm transforms it into a customized Hilorama composition.

<p align="center">
  <table>
    <tr>
      <td align="center"><img src="readme/ai1.jpg" alt="AI-generated source image example 1" width="260" style="border-radius: 50%; object-fit: cover;" /><br /><strong>AI example 1</strong></td>
      <td align="center"><img src="readme/ai2.jpg" alt="AI-generated source image example 2" width="260" style="border-radius: 50%; object-fit: cover;" /><br /><strong>AI example 2</strong></td>
    </tr>
  </table>
</p>

> Warning: this is a free AI model, so it can make mistakes. The best results usually come from simple, generic prompts that the model can interpret easily without strain.

### Save as image and video

The app gives you two ways to preserve the final result: export a still image or record the live generation as a video.

- Image

  The app captures the current state of the generated composition and saves it as a bitmap to the device gallery using Android media storage APIs. This means the image can be taken from the current live or paused state of the Hilorama, not only from a fixed final frame. The file is stored in the Pictures folder, making it easy to access and share the artwork later.

- Video

  The app records the same real-time rendering shown to the user and exports it as an MP4 file. The result is saved in a dedicated Hilorama folder, using MediaCodec and MediaMuxer to preserve the same visual process as the live preview.

### Fade

The app includes a fade window that lets the user slide a vertical mask to reveal part of the original image while the Hilorama generation continues underneath. This creates a direct, one-to-one comparison between the algorithmic output and the original reference, making it easy to evaluate how faithfully the composition follows the source.

<p align="center">
  <table>
    <tr>
      <td align="center"><img src="readme/fade1.jpg" alt="Lady 1" width="260" style="border-radius: 50%; object-fit: cover;" /><br /><strong>Lady 1</strong></td>
      <td align="center"><img src="readme/fade2.jpg" alt="Lady 2" width="260" style="border-radius: 50%; object-fit: cover;" /><br /><strong>Lady 2</strong></td>
    </tr>
  </table>
</p>

Here is an example of the feature with images of two beautiful young women from different time ;)

## Step by step

This mode helps you understand how the drawing is constructed step by step, making the process easier to follow and more intuitive to learn.

<p align="center">
  <table>
    <tr>
      <td align="center"><img src="readme/degree.jpg" alt="Degree numbering diagram" width="300" style="border-radius: 24px; object-fit: cover; box-shadow: 0 10px 20px rgba(0,0,0,0.18);" /></td>
      <td valign="middle" style="padding-left: 18px; text-align: left;">
        Each nail is numbered around the circle in order, starting from 0 and continuing clockwise. The numbers are the nail indices, and every thread is defined by the connection between two of these indices.
        <br /><br />
      </td>
    </tr>
  </table>
</p>

Easy to understand, right? Well, if you ever get lost, you can consult the instructions by pressing the third button in the top bar.

<p align="center">
  <table>
    <tr>
      <td align="center"><img src="readme/step1.jpg" alt="Step by step reference image 1" width="220" style="border-radius: 24px; object-fit: cover; box-shadow: 0 10px 20px rgba(0,0,0,0.18);" /></td>
      <td align="center"><img src="readme/step2.jpg" alt="Step by step reference image 2" width="220" style="border-radius: 24px; object-fit: cover; box-shadow: 0 10px 20px rgba(0,0,0,0.18);" /></td>
    </tr>
  </table>
</p>

The two circles with their corresponding numbers represent the two nails that hold the thread to be drawn. Each number identifies a nail index, and the thread is the connection between those two nails. On the right-side list, you can review the sequence more clearly and even go back to any previous thread whenever needed.

But what if you need to close the app in the middle of a step-by-step Hilorama creation? This app offers a solution for that problem. See it in the next section.

## Bookmark

In the step-by-step menu, using the second button in the top bar, the bookmark icon, the current work is saved even if the app is closed. This makes it easy to pause the process without losing the progress already made.

<p align="center">
  <img src="readme/bookmark.jpg" alt="Bookmark menu" width="260" style="border-radius: 24px; object-fit: cover; box-shadow: 0 10px 20px rgba(0,0,0,0.18);" />
</p>

From this menu, you can load each saved work and continue exactly from the point where you left it. The app keeps the progress of the current Hilorama so you can resume the process naturally and continue drawing from the same thread sequence.

## Performance

String Art is, at its heart, an approximation problem: a set of lines is used to recreate a continuous image with a finite number of segments.

### From equation to approximation

The problem can be modeled as a linear system:

$$
A x = b
$$

Here, the matrix $A$ represents the geometric relationships between anchors and candidate segments, the vector $x$ encodes the decision of which threads should be active, and $b$ represents the target image or the desired approximation.

In practice, solving this exactly is not feasible for a realistic composition: the number of possible combinations grows explosively, and the exact solution becomes computationally intractable.

### Why greedy is the key

Instead of solving the full problem at once, the algorithm chooses the next segment that yields the largest improvement. With $n$ anchors there are

$$
\frac{n(n-1)}{2}
$$

possible threads to consider.

The goal is then to choose the thread that minimizes the residual error:

$$
\min \|A x - b\|
$$

This greedy strategy trades perfect optimization for a fast, iterative approximation that is good enough to produce compelling artistic results.

### Bresenham algorithm

Before the greedy decision is applied, the app needs a reliable way to draw a line between two discrete points. That is where the Bresenham algorithm becomes essential.

Bresenham is an integer-only line rasterization method used to determine which pixels should be activated when drawing a straight segment on a grid. In Hilorama, this is critical because the final String Art composition is built from many small geometric segments, and each one must be mapped accurately onto the image lattice.

<p align="center">
  <img src="readme/bresenham-large-line.CWYLaWwl_1RtQSx.webp" alt="Bresenham line drawing reference" width="420" />
</p>

The result is a fast, precise, and memory-efficient rendering step that enables the greedy optimization to operate over real image data without wasting computational effort on expensive floating-point rasterization.

### Why C++?

The expensive part of Hilorama is not the UI; it is the repeated math behind each approximation step. We evaluate many candidate segments, update the residual error, and redraw the image thousands of times as the composition improves.

That is where C++ helps: it keeps the hot loops leaner and gives tighter control over memory and CPU usage. Kotlin remains the right tool for the Android interface, but the heavy numerical work is delegated to the native engine.

## The end

<p align="center">
      <img src="readme/uh.jpg" alt="University of Havana" width="220" style="border-radius: 50%; object-fit: cover; box-shadow: 0 10px 20px rgba(0,0,0,0.18);" />
      <img src="readme/matcom.jpg" alt="MATCOM" width="220" style="border-radius: 50%; object-fit: cover; box-shadow: 0 10px 20px rgba(0,0,0,0.18);" />
</p>

<p align="center">
  Made with love by a University of Havana student, thank you for stopping by and leave a star if you like the project! ⭐
</p>

