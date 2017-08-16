# DeepImagePreview-Project

## What
The idea was to create an application which is able to recognize some text using the camera of the device, filter out the text that is not related to "Food", load some image from the text and finally display them to the user. 

One of the use case for this application, is when you are at the restaurant and you want to know how a dish listed in the menu, looks like before orderting it.

## How
In order to achieve the expected result, I had to combine different technologies during the development:

- [Text Recognition API](https://developers.google.com/vision/text-overview) (for detecting the text from the camera of the device)
- Machine learning (for filtering our the text that is not Food related)
- [Google Custom Search Engine](https://developers.google.com/custom-search/docs/overview) (for performing queries to the Google Images service and retrieve the actual images)

## Problematics
It seems everything so easy when you explain it but, unfortunately, there are a few impediments for the realization of this project

 1. **It's really difficult to create a reliable machine learning model that is trained and able to filter out text that is not food related** due to the dimensions of the dataset for food topics and to the scalability of the project (needs to work in different languages with as many cuisines as possible)

 2. **The Google Custom Search Engine is not free.**
 
 For more info about the machine learning approach check here!

## How to use it
In order to use this application you have to create a Google Custom Search Engine enabling the [Image Search](https://support.google.com/customsearch//answer/2630972?hl=en&ctx=topic&topic=2568895) and add the ID of your engine to the build.gradle file.

You also have to add the Google APi Key to the same file as per image below.

IMAGE HERE




