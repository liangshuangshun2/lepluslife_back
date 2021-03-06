//package com.jifenke.lepluslive.web.rest;
//
//import com.jifenke.lepluslive.Application;
//import com.jifenke.lepluslive.barcode.BarcodeConfig;
//import com.jifenke.lepluslive.barcode.service.BarcodeService;
//import com.jifenke.lepluslive.filemanage.service.FileImageService;
//import com.jifenke.lepluslive.global.config.Constants;
//import com.jifenke.lepluslive.global.util.MvUtil;
//import com.jifenke.lepluslive.merchant.domain.entities.Merchant;
//import com.jifenke.lepluslive.merchant.domain.entities.MerchantUser;
//import com.jifenke.lepluslive.merchant.repository.MerchantRepository;
//import com.jifenke.lepluslive.merchant.service.MerchantService;
//import com.jifenke.lepluslive.partner.domain.entities.Partner;
//import com.jifenke.lepluslive.score.repository.ScoreARepository;
//import com.jifenke.lepluslive.score.repository.ScoreBRepository;
//import com.jifenke.lepluslive.user.repository.LeJiaUserRepository;
//import com.jifenke.lepluslive.user.repository.WeiXinUserRepository;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.IntegrationTest;
//import org.springframework.boot.test.SpringApplicationConfiguration;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.web.WebAppConfiguration;
//
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.util.Date;
//import java.util.List;
//
//import javax.inject.Inject;
//
///**
//* Created by wcg on 16/4/15.
//*/
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = Application.class)
//@WebAppConfiguration
//@IntegrationTest
//@ActiveProfiles({Constants.SPRING_PROFILE_DEVELOPMENT})
//public class ttt {
//
//
//  @Inject
//  private WeiXinUserRepository weiXinUserRepository;
//
//  @Inject
//  private LeJiaUserRepository leJiaUserRepository;
//
//  @Inject
//  private ScoreARepository scoreARepository;
//
//  @Inject
//  private MerchantRepository merchantRepository;
//
//  @Inject
//  private MerchantService merchantService;
//
//  @Inject
//  private BarcodeService barcodeService;
//
//  private String barCodeRootUrl="http://lepluslive-barcode.oss-cn-beijing.aliyuncs.com";
//
//  @Inject
//  private FileImageService fileImageService;
//
//
//  @Test
//  public void tttt(){
//    List<Merchant> merchants = merchantRepository.findAll();
//    for(Merchant merchant:merchants){
//      byte[]
//          bytes =
//          new byte[0];
//      try {
//        bytes = barcodeService.qrCode(Constants.MERCHANT_URL + merchant.getMerchantSid(),
//                                      BarcodeConfig.QRCode.defaultConfig());
//      } catch (InterruptedException e) {
//        e.printStackTrace();
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//      String filePath = MvUtil.getFilePath(Constants.BAR_CODE_EXT);
//
//      merchant.setQrCodePicture(barCodeRootUrl + "/" + filePath);
//      final byte[] finalBytes = bytes;
//        fileImageService.SaveBarCode(finalBytes, filePath);
//      merchantRepository.save(merchant);
//    }
//
//
//  }
//
//////  public static void main(String[] args) {
//////    int x[][] = new int[9][9];
//////    for(int i=0;i<9;i++){
//////      for(int y=0;y<9;y++){
//////        x[i][y]=new Random().nextInt(2);
//////      }
//////    }
//////    Scanner input = new Scanner(System.in);
//////    int a = input.nextInt();
//////    int b = input.nextInt();
//////    int n = input.nextInt();
//////
//////    for(int z=1;z<n;z++){
//////      int m = x[a][b];
//////      int a1 = x[a-1][b];
//////      int a2 = x[a+1][b];
//////      int a3 = x[a][b+1];
//////      int a4 = x[a][b-1];
//////
//////
//////
//////    }
////
////
////
////  }
//
//
//
//
//}
