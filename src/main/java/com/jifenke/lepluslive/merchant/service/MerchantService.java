package com.jifenke.lepluslive.merchant.service;

import com.jifenke.lepluslive.barcode.BarcodeConfig;
import com.jifenke.lepluslive.barcode.service.BarcodeService;
import com.jifenke.lepluslive.filemanage.service.FileImageService;
import com.jifenke.lepluslive.global.config.Constants;
import com.jifenke.lepluslive.global.util.MD5Util;
import com.jifenke.lepluslive.global.util.MvUtil;
import com.jifenke.lepluslive.merchant.domain.criteria.MerchantCriteria;
import com.jifenke.lepluslive.merchant.domain.entities.City;
import com.jifenke.lepluslive.merchant.domain.entities.Merchant;
import com.jifenke.lepluslive.merchant.domain.entities.MerchantType;
import com.jifenke.lepluslive.merchant.domain.entities.MerchantUser;
import com.jifenke.lepluslive.merchant.domain.entities.MerchantWallet;
import com.jifenke.lepluslive.merchant.repository.MerchantProtocolRepository;
import com.jifenke.lepluslive.merchant.repository.MerchantRepository;
import com.jifenke.lepluslive.merchant.repository.MerchantTypeRepository;
import com.jifenke.lepluslive.merchant.repository.MerchantUserRepository;
import com.jifenke.lepluslive.merchant.repository.MerchantWalletRepository;
import com.jifenke.lepluslive.order.domain.entities.FinancialStatistic;
import com.jifenke.lepluslive.user.domain.entities.RegisterOrigin;
import com.jifenke.lepluslive.user.repository.LeJiaUserRepository;
import com.jifenke.lepluslive.user.repository.RegisterOriginRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Created by wcg on 16/3/17.
 */
@Service
@Transactional(readOnly = true)
public class MerchantService {

  @Inject
  private MerchantRepository merchantRepository;

  @Inject
  private MerchantTypeRepository merchantTypeRepository;

  @Inject
  private RegisterOriginRepository registerOriginRepository;

  @Inject
  private MerchantUserRepository merchantUserRepository;

  @Inject
  private BarcodeService barcodeService;

  @Inject
  private FileImageService fileImageService;

  @Inject
  private MerchantWalletRepository merchantWalletRepository;

  @Inject
  private MerchantProtocolRepository merchantProtocolRepository;

  @Inject
  private LeJiaUserRepository leJiaUserRepository;

  @Value("${bucket.ossBarCodeReadRoot}")
  private String barCodeRootUrl;

  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public Page findMerchantsByPage(MerchantCriteria merchantCriteria, Integer limit) {
    Sort sort = new Sort(Sort.Direction.DESC, "sid");
    return merchantRepository
        .findAll(getWhereClause(merchantCriteria),
                 new PageRequest(merchantCriteria.getOffset() - 1, limit, sort));
  }

  public Merchant findMerchantById(Long id) {
    return merchantRepository.findOne(id);
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public void createMerchant(Merchant merchant) {
    if (merchant.getId() != null) {
      throw new RuntimeException("新建商户ID不能存在");
    }
    merchant.setSid((int) merchantRepository.count());
    String merchantSid = MvUtil.getMerchantSid();
    while (merchantRepository.findByMerchantSid(merchantSid).isPresent()) {
      merchantSid = MvUtil.getMerchantSid();
    }
    merchant.setMerchantSid(merchantSid);
    byte[]
        bytes =
        new byte[0];
    try {
      bytes = barcodeService.qrCode(Constants.MERCHANT_URL + merchant.getMerchantSid(),
                                    BarcodeConfig.QRCode.defaultConfig());
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    String filePath = MvUtil.getFilePath(Constants.BAR_CODE_EXT);

    merchant.setQrCodePicture(barCodeRootUrl + "/" + filePath);
    final byte[] finalBytes = bytes;
    new Thread(() -> {
      fileImageService.SaveBarCode(finalBytes, filePath);
    }).start();
    merchantRepository.save(merchant);
    RegisterOrigin registerOrigin = new RegisterOrigin();
    registerOrigin.setOriginType(3);
    registerOrigin.setMerchant(merchant);
    MerchantWallet merchantWallet = new MerchantWallet();
    merchantWallet.setMerchant(merchant);
    merchant.getMerchantProtocols().stream().map(merchantProtocol -> {
      merchantProtocol.setMerchant(merchant);
      merchantProtocolRepository.save(merchantProtocol);
      return merchantProtocol;
    }).collect(Collectors.toList());
    merchantWalletRepository.save(merchantWallet);
    registerOriginRepository.save(registerOrigin);
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public void editMerchant(Merchant merchant) {
    Merchant origin = merchantRepository.findOne(merchant.getId());
    origin.getMerchantProtocols().stream().map(merchantProtocol -> {
      merchantProtocolRepository.delete(merchantProtocol);
      return null;
    }).collect(Collectors.toList());
//    origin.
    String sid = origin.getMerchantSid();
    if (origin == null) {
      throw new RuntimeException("不存在的商户");
    }
    origin.setLjBrokerage(merchant.getLjBrokerage());
    origin.setLjCommission(merchant.getLjCommission());
    origin.setName(merchant.getName());
    origin.setLocation(merchant.getLocation());
    origin.setState(merchant.getState());
    origin.setPartner(merchant.getPartner());
    origin.setArea(merchant.getArea());
    origin.setUserLimit(merchant.getUserLimit());
    origin.setCity(merchant.getCity());
    origin.setContact(merchant.getContact());
    origin.setPayee(merchant.getPayee());
    origin.setCycle(merchant.getCycle());
    origin.setMerchantBank(merchant.getMerchantBank());
    origin.setMerchantPhone(merchant.getMerchantPhone());
    origin.setMerchantProtocols(merchant.getMerchantProtocols());
    origin.setScoreARebate(merchant.getScoreARebate());
    origin.setScoreBRebate(merchant.getScoreBRebate());
    origin.setMerchantType(merchant.getMerchantType());
    origin.setPicture(merchant.getPicture());
    origin.setReceiptAuth(merchant.getReceiptAuth());
    origin.setPartnership(merchant.getPartnership());
    origin.setMemberCommission(merchant.getMemberCommission());
//    try {
//      BeanUtils.copyProperties(origin, merchant);
//    } catch (IllegalAccessException e) {
//      e.printStackTrace();
//    } catch (InvocationTargetException e) {
//      e.printStackTrace();
//    }
    long l = merchant.getId();
    origin.setSid((int) l);
    origin.setMerchantSid(sid);
    origin.getMerchantProtocols().stream().map(merchantProtocol -> {
      merchantProtocol.setMerchant(origin);
      merchantProtocolRepository.save(merchantProtocol);
      return null;
    }).collect(Collectors.toList());
    merchantRepository.save(origin);
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public void disableMerchant(Long id) {
    Merchant merchant = merchantRepository.findOne(id);
    if (merchant == null) {
      throw new RuntimeException("不存在的商户");
    }
    merchant.setState(0);
    merchantRepository.save(merchant);
  }

  public List<MerchantType> findAllMerchantTypes() {
    return merchantTypeRepository.findAll();
  }

  public static Specification<Merchant> getWhereClause(MerchantCriteria merchantCriteria) {
    return new Specification<Merchant>() {
      @Override
      public Predicate toPredicate(Root<Merchant> r, CriteriaQuery<?> q,
                                   CriteriaBuilder cb) {
        Predicate predicate = cb.conjunction();
        if (merchantCriteria.getPartnership() != null) {
          predicate.getExpressions().add(
              cb.equal(r.get("partnership"),
                       merchantCriteria.getPartnership()));
        }

        if (merchantCriteria.getMerchantType() != null) {
          predicate.getExpressions().add(
              cb.equal(r.get("merchantType"),
                       new MerchantType(merchantCriteria.getMerchantType())));
        }

        if (merchantCriteria.getMerchant() != null && merchantCriteria.getMerchant() != "") {
          if (merchantCriteria.getMerchant().matches("^\\d{1,6}$")) {
            predicate.getExpressions().add(
                cb.like(r.get("merchantSid"),
                        "%" + merchantCriteria.getMerchant() + "%"));
          } else {
            predicate.getExpressions().add(
                cb.like(r.get("name"),
                        "%" + merchantCriteria.getMerchant() + "%"));
          }
        }

        if (merchantCriteria.getReceiptAuth() != null) {
          predicate.getExpressions().add(
              cb.equal(r.get("receiptAuth"),
                       merchantCriteria.getReceiptAuth()));
        }

        if (merchantCriteria.getStoreState() != null) {
          predicate.getExpressions().add(
              cb.equal(r.get("state"),
                       merchantCriteria.getStoreState()));
        }

        if (merchantCriteria.getStartDate() != null && merchantCriteria.getStartDate() != "") {
          predicate.getExpressions().add(
              cb.between(r.get("createDate"), new Date(merchantCriteria.getStartDate()),
                         new Date(merchantCriteria.getEndDate())));
        }

        if (merchantCriteria.getCity() != null) {
          predicate.getExpressions().add(
              cb.equal(r.get("city"),
                       new City(merchantCriteria.getCity())));
        }

        return predicate;
      }
    };
  }

  public List<MerchantUser> findMerchantUsersByMerchant(Merchant merchant) {
    return merchantUserRepository.findAllByMerchant(merchant);
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public void deleteMerchantUser(Long id) {
    merchantUserRepository.delete(id);
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public void editMerchantUser(MerchantUser merchantUser) {
    Merchant merchant = merchantRepository.findOne(merchantUser.getMerchant().getId());
    merchantUser.setPassword(MD5Util.MD5Encode(merchantUser.getPassword(), "UTF-8"));
    merchantUser.setMerchant(merchant);
    merchantUserRepository.save(merchantUser);
  }

  public MerchantUser getMerchantUserById(Long id) {
    return merchantUserRepository.findOne(id);
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public Merchant qrCodeManage(Long id) {
    Merchant merchant = merchantRepository.findOne(id);

    if (merchant.getQrCodePicture() == null) {
      byte[]
          bytes =
          new byte[0];
      try {
        bytes = barcodeService.qrCode(Constants.MERCHANT_URL + merchant.getMerchantSid(),
                                      BarcodeConfig.QRCode.defaultConfig());
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      String filePath = MvUtil.getFilePath(Constants.BAR_CODE_EXT);
      fileImageService.SaveBarCode(bytes, filePath);

      merchant.setQrCodePicture(barCodeRootUrl + "/" + filePath);

      merchantRepository.save(merchant);
    }

    return merchant;
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public List<MerchantUser> findMerchantUserByMerchant(Merchant merchant) {
    return merchantUserRepository.findAllByMerchant(merchant);
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public void openStore(Merchant merchant) {
    Merchant origin = merchantRepository.findOne(merchant.getId());

    origin.setState(1);

    origin.setLat(merchant.getLat());

    origin.setLng(merchant.getLng());

    origin.setOfficeHour(merchant.getOfficeHour());

    origin.setPhoneNumber(merchant.getPhoneNumber());

    merchantRepository.save(origin);
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public void closeStore(Long merchantId) {
    Merchant origin = merchantRepository.findOne(merchantId);

    origin.setState(0);

    merchantRepository.save(origin);
  }

  //获取每个合伙人的锁定会员数
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public List<Integer> findBindLeJiaUsers(List<Merchant> merchants) {

    List<Integer> binds = new ArrayList<>();
    int count = 0;
    for (Merchant merchant : merchants) {
      count = leJiaUserRepository.countByBindMerchant(merchant.getId());
      binds.add(count);
    }
    return binds;
  }


  public MerchantWallet findMerchantWalletByMerchant(Merchant merchant) {
    return merchantWalletRepository.findByMerchant(merchant);
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public void changeMerchantWalletTotalTransferMoney(FinancialStatistic financialStatistic) {
    MerchantWallet merchantWallet = findMerchantWalletByMerchant(financialStatistic.getMerchant());
    merchantWallet.setTotalTransferMoney(
        merchantWallet.getTotalTransferMoney() + financialStatistic.getTransferPrice());
    merchantWalletRepository.save(merchantWallet);
  }
}
