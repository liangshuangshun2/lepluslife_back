package com.jifenke.lepluslive.score.service;

import com.jifenke.lepluslive.score.domain.entities.ScoreB;
import com.jifenke.lepluslive.score.domain.entities.ScoreBDetail;
import com.jifenke.lepluslive.score.repository.ScoreBDetailRepository;
import com.jifenke.lepluslive.score.repository.ScoreBRepository;
import com.jifenke.lepluslive.user.domain.entities.LeJiaUser;
import com.jifenke.lepluslive.user.domain.entities.WeiXinUser;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by wcg on 16/3/18.
 */
@Service
@Transactional(readOnly = true)
public class ScoreBService {

  @Inject
  private ScoreBRepository scoreBRepository;

  @Inject
  private ScoreBDetailRepository scoreBDetailRepository;

  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public ScoreB findScoreBByWeiXinUser(LeJiaUser leJiaUser) {
    return scoreBRepository.findByLeJiaUser(leJiaUser);
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public List<ScoreBDetail> findAllScoreBDetail(WeiXinUser weiXinUser) {
    return scoreBDetailRepository.findAllByScoreB(findScoreBByWeiXinUser(weiXinUser.getLeJiaUser()));
  }

  @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
  public void paySuccess(LeJiaUser leJiaUser, Long totalScore) {
    ScoreB scoreB = findScoreBByWeiXinUser(leJiaUser);
    if (scoreB.getScore() - totalScore > 0) {
      scoreB.setScore(scoreB.getScore() - totalScore);
      ScoreBDetail scoreBDetail = new ScoreBDetail();
      scoreBDetail.setOperate("乐+商城消费");
      scoreBDetail.setScoreB(scoreB);
      scoreBDetail.setNumber(-totalScore);
      scoreBDetailRepository.save(scoreBDetail);
      scoreBRepository.save(scoreB);
    }else{
      throw new RuntimeException("积分不足");
    }
  }
}
