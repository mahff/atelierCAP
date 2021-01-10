#include "opencv2/opencv.hpp"
#include "iostream"
#include "math.h"

using namespace cv;
using namespace std;

#define DISTANCE 140


float MLUT[360];


Point barycentre(Mat gray)
{
    Point p;
    int rows = gray.rows;
    int cols = gray.cols;

    float cumulRows = 0, cumulCols = 0, baryX = 0, baryY = 0, pix, nbOfPix = 0;

    for(int i = 1; i<rows-1 ; i++){
        for(int j = 1; j<cols-1 ; j++){
            pix = gray.at<uchar>(i, j);
            if (pix > 50){
                nbOfPix++;
                cumulRows += i;
                cumulCols += j;
            }
        }
    }

    baryX = cumulCols / nbOfPix;
    baryY = cumulRows / nbOfPix;
    p.x = baryX;
    p.y = baryY;

    return p;
}

/**
 * \fn int maskOnPix(Mat frame, int masque[3][3], int i, int j)
 * \brief Convolue un pixel par un masque donné
 *
 * \param[in] frame Image en niveau de gris
 * \param[in] masque Masque à utiliser pour la convolution
 * \param[in] i Coordonnée en ordonnée du pixel à convoluer
 * * \param[in] j Coordonnée en absisse du pixel à convoluer
 */
int maskOnPix(Mat frame, int masque[3][3], int i, int j){

    int res = masque[0][0]*frame.at<uchar>(i-1, j-1)
                + masque[0][1]*frame.at<uchar>(i-1, j)
                + masque[0][2]*frame.at<uchar>(i-1, j+1)
                + masque[1][0]*frame.at<uchar>(i, j-1)
                + masque[1][1]*frame.at<uchar>(i, j)
                + masque[1][2]*frame.at<uchar>(i, j+1)
                + masque[2][0]*frame.at<uchar>(i+1, j-1)
                + masque[2][1]*frame.at<uchar>(i+1, j)
                + masque[2][2]*frame.at<uchar>(i+1, j+1);
    return res;
}


Mat gradientFunction(Mat gray){

    int rows = gray.rows;
    int cols = gray.cols;

    Mat gradient(rows, cols, CV_8UC1);
    Mat gradientX(rows, cols, CV_64F);
    Mat gradientY(rows, cols, CV_64F);
    Mat theta(rows, cols, CV_64F);
    Mat beta(rows, cols, CV_64F);


    float lut[360] = {0};
    int cnt[360] = {0};

    float tmp, tmpx, tmpy;
    int masqueGradientVertical[3][3] = {{-1,-2,-1}, {0,0,0}, {1,2,1}};
    int masqueGradientHorizontal[3][3] = {{-1,0,1}, {-2,0,2}, {-1,0,1}};


    float min, max;
    min = 512;
    max = 0;
    Point centre = barycentre(gray);

    for(int i = 1; i<rows-1 ; i++){
        for(int j = 1; j<cols-1 ; j++){
            tmpy = maskOnPix(gray, masqueGradientVertical, i, j);
            tmpy /= 4;

            tmpx = maskOnPix(gray, masqueGradientHorizontal, i, j);
            tmpx /= 4;

            tmp = sqrt((tmpx*tmpx)+(tmpy*tmpy));

            if(tmp > max) max = tmp;
            else if(tmp < min) min = tmp;

            if (tmp>=255)
            {
                gradient.at<uchar>(i, j) = tmp;
                gradientX.at<float>(i, j) = tmpx;
                gradientY.at<float>(i, j) = tmpy;
            }else{
                gradient.at<uchar>(i, j) = 0;
                gradientX.at<float>(i, j) = 0;
                gradientY.at<float>(i, j) = 0;
            }
        }
    }

    for(int i = 1; i<rows-1 ; i++){
        for(int j = 1; j<cols-1 ; j++){
            if (gradient.at<uchar>(i, j) != 0)
            {
                int indice = nearbyint(atan2(gradientY.at<float>(i, j), gradientX.at<float>(i, j))*180/M_PI +180);
                theta.at<float>(i, j) = (atan2(gradientY.at<float>(i, j), gradientX.at<float>(i, j))*180/M_PI +180);

                beta.at<float>(i,j) = atan2(centre.y-i, centre.x-j);
                lut[indice] += beta.at<float>(i,j);
                cnt[indice]++;
            }
            else
            {
                theta.at<float>(i, j) = 0;
                beta.at<float>(i,j) = 0;
            }
        }
    }
    for(int i = 0; i<360 ; i++){
            if(cnt[i] != 0){
                lut[i] /= cnt[i];
            }

        }
        memcpy(MLUT, lut, sizeof(lut));

    for(int i = 1; i<rows-1 ; i++){
        for(int j = 1; j<cols-1 ; j++){
            gradient.at<uchar>(i, j) = (gradient.at<uchar>(i, j)+abs(min))*255.0/(max+abs(min));
        }
    }

    return gradient;
}



Point findBarycentreWithMLUT(Mat gray)
{
  int rows = gray.rows;
  int cols = gray.cols;

  Mat gradient(rows, cols, CV_8UC1);
  Mat gradientX(rows, cols, CV_64F);
  Mat gradientY(rows, cols, CV_64F);

  Mat vote(rows, cols, CV_8UC1);

  float tmp, tmpx, tmpy;
  int masqueGradientVertical[3][3] = {{-1,-2,-1}, {0,0,0}, {1,2,1}};
  int masqueGradientHorizontal[3][3] = {{-1,0,1}, {-2,0,2}, {-1,0,1}};


  float min, max, theta, a, b;
  min = 512;
  max = 0;

  for(int i = 1; i<rows-1 ; i++){
      for(int j = 1; j<cols-1 ; j++){
          vote.at<uchar>(i, j) = 0;
          tmpy = maskOnPix(gray, masqueGradientVertical, i, j);
          tmpy /= 4;

          tmpx = maskOnPix(gray, masqueGradientHorizontal, i, j);
          tmpx /= 4;

          tmp = sqrt((tmpx*tmpx)+(tmpy*tmpy));

          if(tmp > max) max = tmp;
          else if(tmp < min) min = tmp;

          if (tmp>=255)
          {
              gradient.at<uchar>(i, j) = tmp;
              gradientX.at<float>(i, j) = tmpx;
              gradientY.at<float>(i, j) = tmpy;

              // find line from theta
              int theta = nearbyint(atan2(gradientY.at<float>(i, j), gradientX.at<float>(i, j))*180/M_PI +180);
              float beta = MLUT[theta];
              a = tan(beta);
              b = i - a*j;
              if(theta>90 && theta<270){

                for (size_t x = j; x < j+DISTANCE; x++) {
                  int line = nearbyint(a*x+b);
                  // Verify if value of line and x are outside the image
                  if((line >=0 && line<=cols) && (x>=0 && x<=cols))
                    vote.at<uchar>(line, x)+=1;
                }
              }else{
                for (size_t x = j-DISTANCE; x < j; x++) {
                  int line = nearbyint(a*x+b);
                  // Verify if value of line and x are outside the image
                  if((line >=0 && line<=cols) && (x>=0 && x<=cols))
                    vote.at<uchar>(line, x)+=1;
                }

            }
          }else{
              gradient.at<uchar>(i, j) = 0;
              gradientX.at<float>(i, j) = 0;
              gradientY.at<float>(i, j) = 0;
          }
      }
  }

  max = 0;
  Point maxLoc(0,0);
  // find the maximun value in the matrix
  for(int i = 1; i<rows-1 ; i++){
      for(int j = 1; j<cols-1 ; j++){
        if (vote.at<uchar>(i, j)> max) {
          maxLoc.x = j;
          maxLoc.y = i;
          max = vote.at<uchar>(i, j);
        }
      }
  }

  // Normalization 
  for(int i = 1; i<rows-1 ; i++){
      for(int j = 1; j<cols-1 ; j++){
          vote.at<uchar>(i, j) = (vote.at<uchar>(i, j)+abs(min))*255.0/(max+abs(min));
      }
  }

  cv::imshow("Accumulation", vote);
  return maxLoc;

}

int main() {


    Mat frame = imread("cercle_noir_fond_blanc_2.png", IMREAD_GRAYSCALE);
    frame = gradientFunction(frame);
    Point b = barycentre(frame);
    cv::circle(frame, Point(b.x, b.y),10,(255, 255, 255),10);
    cv::imshow("Barycenter", frame);


    Mat frameT = imread("cercle_blanc_fond_noir.png", IMREAD_GRAYSCALE);
    Point vote = findBarycentreWithMLUT(frameT);
    cv::circle(frameT, Point(vote.x, vote.y),10,(255, 0, 0),10);
    cout <<" Point d'Accumulation cercle " <<vote << endl;
    cv::imshow("Vote Cercle", frameT);


    Mat Ellipse = imread("elipse_noir_fond_blanc_2.png", IMREAD_GRAYSCALE);
    Ellipse = gradientFunction(Ellipse);
    Point bEllipse = barycentre(Ellipse);
    cv::circle(Ellipse, Point(bEllipse.x, bEllipse.y),10,(255, 255, 255),10);
    cv::imshow("Barycenter Ellipse", Ellipse);

    Mat EllipseT = imread("elipse_blanc_fond_noir.png", IMREAD_GRAYSCALE);
    Point voteEllipse = findBarycentreWithMLUT(EllipseT);
    cv::circle(EllipseT, Point(voteEllipse.x, voteEllipse.y),10,(255, 0, 0),10);
    cout <<" Point d'Accumulation ellipse " << voteEllipse << endl;
    cv::imshow("Vote Ellipse", EllipseT);

    while (1) {

      if (cv::waitKey(10) >= 0){

          break;
      }
  }


    return 0;
}
