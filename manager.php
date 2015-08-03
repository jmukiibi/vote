<?php

	
	
	include_once('include/config.php');
	
//if(isset($_REQUEST['base64'])){ 
      
      header('Content-Type: bitmap; charset=utf-8');
	//error_reporting(E_ALL);
    // Get image string posted from Android App
    $base=$_POST['base64'];
    
    // Get file name posted from Android App
    $filename = $_POST['filename'];
    
    $subCounty = $_POST['subCounty'];
    echo 'jonathan2';
    $stationNumber = $_POST['stationNumber'];
    
    $pollStation = $_POST['pollStation'];
    
    $vote = $_POST['vote'];
    
    // Decode Image
    $binary=base64_decode($base);
    
     echo 'jonathan';
    // Images will be saved under 'www/imgupload/uplodedimages' folder
    $file = fopen('uploadedimages/'.$filename, 'wb');
    
    // Create File
    fwrite($file, $binary);
    fclose($file);
   // echo 'Image upload complete, Please check your php file directory';
   $file_path='uploadedimages/'+$filename;
	
  $query="INSERT INTO polling(subCounty,stationNumber,pollStation,imagePath,votes) VALUES('$subCounty','$stationNumber','$pollStation','$file_path','$vote')";
    $res=mysql_query($query);    
    if($res > 0){
	
   echo 'Image upload complete, Please check your php file directory';

    }else{
         $response=mysql_error();
         echo  $response;
    }
    
        
    else{
$response["error"] = true;
echo json_encode($response);

    }
?>

 
     
