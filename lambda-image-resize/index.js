const AWS = require('aws-sdk');
const sharp = require('sharp');
const s3 = new AWS.S3();

exports.handler = async (event, context, callback) => {
    const bucket = event.Records[0].s3.bucket.name;
    const key = decodeURIComponent(event.Records[0].s3.object.key.replace(/\+/g, ' '));
    console.log(`* * * bucket: ${bucket} / key: ${key}`);

    // 지원 파일 확장자가 아닌 경우
    const fileType = key.split('.').pop().toLowerCase();
    let formatType = '';
    console.log(`* * * fileType: ${fileType}`);

    if (fileType !== 'jpg' && fileType !== 'jpeg' && fileType !== 'png') {
        console.log(`Unsupported file type: ${fileType}`);
        return;
    }
    else {
        if (fileType === 'jpg' || fileType === 'jpeg') formatType = 'jpeg';
        else if (fileType === 'png') formatType = 'png';
    }
    console.log(`* * * formatType: ${formatType}`);

    // 원본 이미지 호출, 리사이징
    const originalImage = await s3.getObject({ Bucket: bucket, Key: key }).promise();
    let resizedImage_400; // MAIN_THUMBNAIL
    let resizedImage_174; // SUB_THUMBNAIL
    let resizedImage_450; // CONTENT_DETAIL

    if (key.includes('thumb')) {
        try {
            resizedImage_400 = await sharp(originalImage.Body)
                .resize(400, 400, {fit: 'inside'})
                .jpeg({ quality: 100 })
                .toBuffer();
        } catch (error) {
            console.log(error);
            return;
        }
        const newKey_400 = key.replace('original/', 'resize/').replace('thumb/', 'thumb/400/');
        await saveImage(newKey_400, resizedImage_400, bucket, 'jpeg');

        try {
            resizedImage_174 = await sharp(originalImage.Body)
                .resize(174, 174, {fit: 'inside'})
                .toFormat(formatType)
                .toBuffer();
        } catch (error) {
            console.log(error);
            return;
        }
        const newKey_174 = key.replace('original/', 'resize/').replace('thumb/', 'thumb/174/');
        await saveImage(newKey_174, resizedImage_174, bucket, formatType);
    }
    else if (key.includes('content')) {
        try {
            resizedImage_450 = await sharp(originalImage.Body)
                .resize(450, 450, {fit: 'inside'})
                .jpeg({ quality: 80 })
                .toBuffer();
        } catch (error) {
            console.log(error);
            return;
        }
        const newKey_450 = key.replace('original/', 'resize/').replace('content/', 'content/450/');
        await saveImage(newKey_450, resizedImage_450, bucket, 'jpeg');
    }
    else {
        console.log(`* * * key does not include 'thumb' or 'content'.`);
        return;
    }

};

async function saveImage(newKey, resizedImage, bucket, formatType) {
    // 결과 파일 저장
    try {
        await s3.putObject({
            Bucket: bucket,
            Key: newKey,
            Body: resizedImage,
            ContentType: 'image/' + formatType
        }).promise();
    } catch(error) {
        console.log(error);
        return;
    }

    console.log(`Successfully Image resized and uploaded to: ${newKey}`);
}