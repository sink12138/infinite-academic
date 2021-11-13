<template>
  <div>
    <h1>Author</h1>
    <go-back></go-back>
    <Analytics></Analytics>
  </div>
</template>

<script>
import GoBack from '../components/GoBack.vue';
import Analytics from "../components/Analytics.vue";
export default {
    name: "Income",
    components: {Analytics,GoBack},
    data() {
        return {
        }
    },
    mounted(){
      console.log(1);
    },
    methods:{
    }
}

</script>

<style>

</style>