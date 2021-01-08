#include "opencv2/opencv.hpp"
#include "iostream"
#include "math.h"
#include "time.h"
#include <errno.h>
#include <fcntl.h>
#include <string.h>
#include <termios.h>
#include <unistd.h>

using namespace cv;
using namespace std;

#define BLUE 0
#define GREEN 30
#define RED 150
#define WIDTH 320
#define HEIGHT 240
#define H_LIMIT 30
#define L_LIMIT 30
#define NEIBOR_WIDTH 3
#define PORTNAME "/dev/ttyACM0"
#define PI 3.141592

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

/**
 * \fn Mat gradientFunction(Mat gray)
 * \brief Calcule le gradient vertical et horizontal PAS UTILISÉ
 */
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
                //cout << "theta : " << theta.at<float>(i, j) << "| beta : " << beta.at<float>(i,j)  << endl;

                // lut[indice] = lut[indice] + beta.at<float>(i,j);
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
                // cout << "lut[i] after : " << lut[i] << endl;
                // cout << "lut[i] after : " << lut[i] * 180 / M_PI << endl;
                cout << "theta " << i - 180 << " beta " << lut[i] * 180 / M_PI + 180<< endl;
            }

        }


    for(int i = 1; i<rows-1 ; i++){
        for(int j = 1; j<cols-1 ; j++){
            gradient.at<uchar>(i, j) = (gradient.at<uchar>(i, j)+abs(min))*255.0/(max+abs(min));
        }
    }

	return gradient;
}




Mat getCircle()
{
    Mat frame(HEIGHT, WIDTH, CV_8UC1);;
    for (size_t i = 0; i < HEIGHT; i++)
    {
        for (size_t j = 0; j < WIDTH; j++)
        {
            frame.at<uchar>(i, j) = 0;
        }

    }

    cv::circle(frame, Point(WIDTH/2, HEIGHT/2), 10, (255, 255, 255));

    return frame;

}

Mat getELipse()
{
    Mat frame(HEIGHT, WIDTH, CV_8UC1);;
    for (size_t i = 0; i < HEIGHT; i++)
    {
        for (size_t j = 0; j < WIDTH; j++)
        {
            frame.at<uchar>(i, j) = 0;
        }

    }

    cv::ellipse(frame, Point( WIDTH/2, HEIGHT/2 ), Size( 50.0, 100.0 ), 0, 0, 360, Scalar( 255, 0, 0 ), 3, 8 );
    return frame;

}



int main() {
    VideoCapture camera(0, CAP_V4L2);
    camera.set(3, WIDTH);
    camera.set(4, HEIGHT);

    namedWindow("Webcam");
    float maxH = 0, maxV = 0;

    Mat frame, frameT, frameT1, gray, grayT, grayT1, output, outputT, outputT1;
    Mat snapshot;


    camera >> frameT;
    cvtColor(frameT, grayT, COLOR_BGR2GRAY);

    outputT = gradientFunction(grayT);

    //Point p = barycentre(outputT);
    //cout << p.x << " | " << p.y << endl;

    frame = imread("cercle_noir_fond_blanc_2.png", IMREAD_GRAYSCALE);
    frame = gradientFunction(frame);
    Point b = barycentre(frame);
    cv::circle(frame, Point(b.x, b.y),10,(255, 255, 255),10);
    cv::imshow("Webcam", frame);



    while (1) {
        //  //camera >> frameT1;
        //  frameT1 = getCircle();

        //  //cvtColor(frameT1, grayT1, COLOR_BGR2GRAY);
        //  outputT1 = gradientFunction(grayT1);
        // cout << "Cam" << endl;
        //  outputT = outputT1;
        // // outputT.convertTo(outputT, CV_8UC1);
        // // outputT1.convertTo(outputT1, CV_8UC1);

        // cv::imshow("Webcam", outputT);
        // //.convertTo(outputT, CV_32F);

        if (cv::waitKey(10) >= 0){

            break;
        }
    }


    return 0;
}
