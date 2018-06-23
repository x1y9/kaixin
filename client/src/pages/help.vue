<template>
    <q-card>
      <q-card-title>
        听音学谱
        <div slot="right"><q-icon name="place" /> {{version}}</div>
      </q-card-title>
      <q-card-main>
        <p>听音学谱是辅助学习五线谱的APP，和其他APP不同，听音学谱监听你的真实乐器的声音，你可以练习任何乐器，钢琴、吉他、小提琴，甚至用你的嗓子来唱也可以，APP会自动分析音准，并判断你弹的音是否正确。</p>
        <p class="text-faded">感谢我的女儿NoNo，希望你天天快乐~</p>
      </q-card-main>
      <q-card-separator />
      <q-card-media>
        <!-- <img src="~assets/icon.png"> -->
      </q-card-media>      
      <!-- <q-card-actions class="text-center">
        <q-btn flat ><q-icon name="monetization on" />捐助我们<span class="text-faded">，让我们做的更好</span></q-btn>
      </q-card-actions> -->
    </q-card>
</template>

<script>
import { QIcon, QBtn, QCard, QCardTitle, QCardMain, QCardSeparator, QCardActions, QCardMedia } from 'quasar'

export default {
  components: { QIcon, QBtn, QCard, QCardTitle, QCardMain, QCardSeparator, QCardActions, QCardMedia },
  mounted () {
  },
  data () {
    return {
      version: ''
    }
  },
  methods: {
    onGetVersion: function (version) {
      this.version = version
    }
  }
}
</script>

<style lang="stylus">

</style>
