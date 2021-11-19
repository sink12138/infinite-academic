<template>
  <div v-html="text">
  </div>
</template>

<script>
export default {
  data:() =>  ({
    text: "Yongfang Li, et al.\"A review of enterprise social media: visualization of landscape and evolution.\""+
    "<span style='font-style:italic'>Internet Research</span>" +
    "<span style='font-style:italic'>31</span><br/>" +
    ".4(2021): doi:10.1108/INTR-07-2020-0389."
  }),
}

</script>


<style scoped>
.v-card--reveal {
  bottom: 0;
  opacity: 1 !important;
  position: absolute;
  width: 100%;
}
</style>